import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import 'antd/dist/reset.css'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#cf1322',
          colorLink: '#cf1322',
          colorError: '#cf1322',
          borderRadius: 8,
          fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans SC", sans-serif',
          boxShadow: '0 4px 16px rgba(0,0,0,0.08)',
        },
        components: {
          Button: {
            primaryShadow: '0 4px 14px rgba(207,19,34,0.3)',
          },
          Card: {
            boxShadow: '0 4px 16px rgba(0,0,0,0.06)',
          },
        },
      }}
    >
      <App />
    </ConfigProvider>
  </StrictMode>,
)