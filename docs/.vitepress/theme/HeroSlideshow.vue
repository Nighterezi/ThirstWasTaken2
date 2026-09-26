<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { withBase } from 'vitepress'

// The home page's hero image: a few in-game shots that cycle on their own.
const slides = [
  { src: '/screenshots/water/iron-hanging-pot.png', alt: 'An Iron Hanging Pot of water boiling over a campfire', label: 'Hanging Pots' },
  { src: '/screenshots/water/jade-water.png', alt: 'Jade showing the grade of the water under the crosshair', label: 'Water grades with Jade' },
  { src: '/screenshots/integrations/create/create-sand-filter-goggles.png', alt: "Engineer's Goggles showing Murky water entering the Sand Filter and Clean water leaving it", label: 'Create Sand Filter' }
]

const INTERVAL_MS = 3000

const active = ref(0)
let timer: number | undefined
let reduceMotion: MediaQueryList | undefined

function stop() {
  if (timer !== undefined) {
    window.clearInterval(timer)
    timer = undefined
  }
}

function start() {
  stop()
  // Nothing moves for a reader who asked the system for less motion; the dots still work.
  if (reduceMotion?.matches) return
  timer = window.setInterval(() => {
    active.value = (active.value + 1) % slides.length
  }, INTERVAL_MS)
}

function select(index: number) {
  active.value = index
  start()
}

onMounted(() => {
  reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)')
  reduceMotion.addEventListener('change', start)
  start()
})

onBeforeUnmount(() => {
  stop()
  reduceMotion?.removeEventListener('change', start)
})
</script>

<template>
  <div
    class="hero-slideshow"
    role="region"
    aria-roledescription="carousel"
    aria-label="ThirstWasTaken2 in game"
    @mouseenter="stop"
    @mouseleave="start"
    @focusin="stop"
    @focusout="start"
  >
    <div class="hero-slideshow-frame">
      <img
        v-for="(slide, index) in slides"
        :key="slide.src"
        :src="withBase(slide.src)"
        :alt="slide.alt"
        class="hero-slideshow-image"
        :class="{ active: index === active }"
        :aria-hidden="index !== active"
        :loading="index === 0 ? 'eager' : 'lazy'"
      >
      <span class="hero-slideshow-label">{{ slides[active].label }}</span>
    </div>

    <div class="hero-slideshow-controls" aria-label="Choose an image">
      <button
        v-for="(slide, index) in slides"
        :key="slide.label"
        type="button"
        :class="{ active: index === active }"
        :aria-label="`Show ${slide.label}`"
        :aria-current="index === active ? 'true' : undefined"
        @click="select(index)"
      />
    </div>
  </div>
</template>
