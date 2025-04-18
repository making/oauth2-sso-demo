/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          light: '#4dabf7',
          DEFAULT: '#228be6',
          dark: '#1971c2',
        },
        success: {
          light: '#69db7c',
          DEFAULT: '#40c057',
          dark: '#2b9a3f',
        },
        danger: {
          light: '#ff8787',
          DEFAULT: '#fa5252',
          dark: '#e03131',
        }
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: 0 },
          '100%': { opacity: 1 },
        },
      },
    },
  },
  plugins: [],
}

