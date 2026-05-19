/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        display: ["'Syne'", "sans-serif"],
        body: ["'DM Sans'", "sans-serif"],
      },
      colors: {
        brand: {
          50:  "#f5f3ff",
          100: "#ede9fe",
          500: "#8b5cf6",
          600: "#7c3aed",
          700: "#6d28d9",
          900: "#4c1d95",
        },
        ink: {
          50:  "#0a0a0c",   // changed page background to dark obsidian
          100: "#12131a",
          200: "#1a1c26",
          800: "#e2e8f0",
          900: "#f8fafc",
        },
        obsidian: {
          50:  "#1a1b26",
          100: "#15161f",
          200: "#12131a",
          800: "#08080a",
          900: "#040405",
        }
      },
      boxShadow: {
        'glow': '0 0 20px rgba(124, 58, 237, 0.15)',
        'glow-emerald': '0 0 20px rgba(16, 185, 129, 0.15)',
        'glow-rose': '0 0 20px rgba(244, 63, 94, 0.15)',
      },
      animation: {
        "fade-in":    "fadeIn 0.4s ease forwards",
        "slide-up":   "slideUp 0.5s cubic-bezier(.4,0,.2,1) forwards",
        "pulse-slow": "pulse 3s cubic-bezier(0.4,0,0.6,1) infinite",
      },
      keyframes: {
        fadeIn:  { from: { opacity: 0 }, to: { opacity: 1 } },
        slideUp: { from: { opacity: 0, transform: "translateY(16px)" },
                   to:   { opacity: 1, transform: "translateY(0)" } },
      },
    },
  },
  plugins: [],
};