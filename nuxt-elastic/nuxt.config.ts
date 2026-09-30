// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  runtimeConfig: {
    elasticsearchUrl: process.env.ELASTICSEARCH_URL || "http://127.0.0.1:9200"
  },
  modules: ['@nuxt/ui'],
  css: ['~/assets/css/main.css']
})
