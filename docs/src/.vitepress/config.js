import { defineConfig } from "vitepress"

export default defineConfig({
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
          { text: 'Client-side Routing', link: '/routing' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/Sparky983/komponent' }
    ]
  }
})
