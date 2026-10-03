// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  runtimeConfig: {
    elasticsearchUrl: process.env.ELASTICSEARCH_URL || "http://127.0.0.1:9200",
    databaseUrl: process.env.DATABASE_URL || "postgresql://postgres:password@localhost:5432/postgres"
  },
  modules: ['@nuxt/ui', '@vueuse/nuxt'],
  css: ['~/assets/css/main.css']
})