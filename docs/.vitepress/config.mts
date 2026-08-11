import { defineConfig } from 'vitepress'

const REPO = 'https://github.com/Nighterezi/ThirstWasTaken2'
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
  // Notes and paste sources for maintainers, not pages on the site.
  srcExclude: ['CLAUDE.md', 'MODRINTH.md'],
  themeConfig: {
    logo: '/logo-small.png',
    externalLinkIcon: true,
    socialLinks: [{ icon: 'github', link: REPO }],
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
