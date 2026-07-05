import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  define: { 'process.env.NODE_ENV': JSON.stringify('production') },
  build: {
    outDir: '../src/main/resources/static/react/commenti',
    emptyOutDir: true,
    lib: {
      entry: 'src/main.jsx',
      name: 'CommentiWidget',
      formats: ['iife'],
      fileName: () => 'commenti.js',
    },
  },
})