import { defineConfig } from "vitepress"

export default defineConfig({
  title: "komponent",
  description: "The Komponent documentation",
  themeConfig: {
    sidebar: [
      {
        items: [
          { text: 'Installation', link: '/' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/Sparky983/komponent' }
    ]
  }
})
