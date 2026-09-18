<script setup lang="ts">
import DefaultTheme from 'vitepress/theme'
import HeroSlideshow from './HeroSlideshow.vue'

const { Layout } = DefaultTheme
</script>

<template>
  <Layout>
    <template #home-hero-image>
      <HeroSlideshow />
    </template>
  </Layout>
</template>
