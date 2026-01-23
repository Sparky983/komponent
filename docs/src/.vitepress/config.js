import { defineConfig } from "vitepress"

export default defineConfig({
  base: "/docs/",
  title: "komponent",
  description: "The Komponent documentation",
  themeConfig: {
    sidebar: [
      {
        items: [
          { text: 'Installation', link: '/' },
          { text: 'Your First Component', link: '/component' },
          { text: 'Reactivity', link: '/reactivity' },
          { text: 'Context API', link: '/context' },
          { text: 'DOM Manipulation', link: '/dom-manipulation' },
          { text: 'Client-side Routing', link: '/routing' }
        ]
      }
    ],
    nav: [
      { text: 'API Reference', link: 'https://komponent.sparky983.me/api' }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/Sparky983/komponent' }
    ]
  }
})
