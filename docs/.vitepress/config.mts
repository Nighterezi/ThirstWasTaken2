import { defineConfig } from 'vitepress'

const REPO = 'https://github.com/Nighterezi/ThirstWasTaken2'
const MODRINTH = 'https://modrinth.com/mod/thirst-was-taken-2'
const BASE = process.env.VITEPRESS_BASE || '/'

const enManualSidebar = [
  {
    text: 'Getting Started',
    items: [
      { text: 'Overview', link: '/docs/' },
      { text: 'Installation', link: '/docs/installation' },
      { text: 'FAQ', link: '/docs/faq' }
    ]
  },
  {
    text: 'Features',
    items: [
      { text: 'Thirst and Quenched', link: '/docs/features/thirst-and-quenched' },
      { text: 'Drinking', link: '/docs/features/drinking' },
      { text: 'Water Purity', link: '/docs/features/water-purity' }
    ]
  },
  {
    text: 'Server Guide',
    items: [
      { text: 'Commands', link: '/docs/commands' },
      { text: 'Configuration', link: '/docs/configuration' }
    ]
  }
]

const viManualSidebar = [
  {
    text: 'Bắt đầu',
    items: [
      { text: 'Tổng quan', link: '/vi/docs/' },
      { text: 'Cài đặt', link: '/vi/docs/installation' },
      { text: 'Câu hỏi thường gặp', link: '/vi/docs/faq' }
    ]
  },
  {
    text: 'Tính năng',
    items: [
      { text: 'Khát và Đã khát', link: '/vi/docs/features/thirst-and-quenched' },
      { text: 'Uống nước', link: '/vi/docs/features/drinking' },
      { text: 'Độ sạch của nước', link: '/vi/docs/features/water-purity' }
    ]
  },
  {
    text: 'Hướng dẫn máy chủ',
    items: [
      { text: 'Lệnh', link: '/vi/docs/commands' },
      { text: 'Cấu hình', link: '/vi/docs/configuration' }
    ]
  }
]

export default defineConfig({
  base: BASE,
  title: 'ThirstWasTaken2',
  description: 'Adds a survival thirst bar, drinking, and water purity.',
  cleanUrls: true,
  lastUpdated: true,
  head: [
    ['link', { rel: 'icon', type: 'image/png', href: `${BASE}logo-small.png` }]
  ],
  // Notes, paste sources and developer planning for maintainers, not pages on the site.
  // 'dev/**' keeps docs/dev out of the build: those pages link into src/ and the repo root,
  // which the dead-link check cannot follow.
  srcExclude: ['AGENTS.md', 'MODRINTH.md', 'dev/**'],
  themeConfig: {
    logo: '/logo-small.png',
    externalLinkIcon: true,
    socialLinks: [
      { icon: 'github', link: REPO },
      {
        icon: {
          svg: '<svg role="img" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><title>Modrinth</title><path d="M12.252.004a11.78 11.78 0 0 0-2.5 0C4.333.606.018 5.17.004 10.843c-.014 5.96 4.62 10.875 10.575 11.144a11.95 11.95 0 0 0 3.844-.22 10.975 10.975 0 0 0 2.228-.795l-.337-.872c-.752-.284-1.46-.662-2.115-1.125a9.01 9.01 0 0 1-2.404.328 9.17 9.17 0 0 1-7.05-3.328 9.27 9.27 0 0 1-2.072-6.046c.01-4.72 3.6-8.583 8.317-8.916 3.125-.22 6.136.96 8.212 3.22a9.14 9.14 0 0 1 2.392 6.07c0 .59-.06 1.17-.18 1.74l.92.17c.14-.62.22-1.26.22-1.91A11.17 11.17 0 0 0 20.73 3.86 11.45 11.45 0 0 0 12.252.004Z"/></svg>'
        },
        link: MODRINTH,
        ariaLabel: 'Modrinth'
      }
    ],
    search: {
      provider: 'local'
    }
  },
  locales: {
    root: {
      label: 'English',
      lang: 'en',
      themeConfig: {
        nav: [
          { text: 'Home', link: '/', activeMatch: '^/$' },
          { text: 'Docs', link: '/docs/', activeMatch: '^/docs/' }
        ],
        sidebar: {
          '/docs/': enManualSidebar
        },
        editLink: {
          pattern: `${REPO}/edit/main/docs/:path`,
          text: 'Edit this page on GitHub'
        },
        outline: {
          level: [2, 3],
          label: 'On this page'
        },
        docFooter: {
          prev: 'Previous page',
          next: 'Next page'
        },
        lastUpdated: {
          text: 'Last updated',
          formatOptions: {
            dateStyle: 'medium',
            timeStyle: 'short'
          }
        }
      }
    },
    vi: {
      label: 'Tiếng Việt',
      lang: 'vi',
      description: 'Thêm thanh khát nước, cơ chế uống nước và độ sạch của nước cho sinh tồn.',
      themeConfig: {
        nav: [
          { text: 'Trang chủ', link: '/vi/', activeMatch: '^/vi/$' },
          { text: 'Tài liệu', link: '/vi/docs/', activeMatch: '^/vi/docs/' }
        ],
        sidebar: {
          '/vi/docs/': viManualSidebar
        },
        editLink: {
          pattern: `${REPO}/edit/main/docs/:path`,
          text: 'Chỉnh sửa trang này trên GitHub'
        },
        outline: {
          level: [2, 3],
          label: 'Trên trang này'
        },
        docFooter: {
          prev: 'Trang trước',
          next: 'Trang sau'
        },
        lastUpdated: {
          text: 'Cập nhật lần cuối',
          formatOptions: {
            dateStyle: 'medium',
            timeStyle: 'short'
          }
        },
        returnToTopLabel: 'Về đầu trang',
        sidebarMenuLabel: 'Menu',
        darkModeSwitchLabel: 'Giao diện',
        lightModeSwitchTitle: 'Chuyển sang giao diện sáng',
        darkModeSwitchTitle: 'Chuyển sang giao diện tối'
      }
    }
  }
})
