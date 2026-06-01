const ALLOWED_PAYMENT_HOSTS = new Set([
  'openapi.alipay.com',
  'openapi-sandbox.dl.alipaydev.com',
]);

export const isPaymentHtml = (html: unknown): html is string =>
  typeof html === 'string' && html.includes('<form');

export const submitPaymentHtml = (html: string): boolean => {
  const doc = new DOMParser().parseFromString(html, 'text/html');
  const sourceForm = doc.querySelector('form');
  if (!sourceForm) {
    return false;
  }

  const action = sourceForm.getAttribute('action') || '';
  let actionUrl: URL;
  try {
    actionUrl = new URL(action, window.location.href);
  } catch {
    return false;
  }

  if (!ALLOWED_PAYMENT_HOSTS.has(actionUrl.hostname)) {
    return false;
  }

  const form = document.createElement('form');
  const method = (sourceForm.getAttribute('method') || 'post').toLowerCase();
  form.method = method === 'get' ? 'get' : 'post';
  form.action = actionUrl.toString();
  form.target = sourceForm.getAttribute('target') || '_self';
  form.style.display = 'none';

  sourceForm.querySelectorAll<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>(
    'input[name], textarea[name], select[name]'
  ).forEach((field) => {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = field.name;
    input.value = field.value;
    form.appendChild(input);
  });

  document.body.appendChild(form);
  form.submit();
  form.remove();
  return true;
};
