/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#16233D',
        'ink-soft': '#5B6478',
        paper: '#F7F6F2',
        trust: '#2554C7',
        'trust-soft': '#E8EEFC',
        drop: '#1C8A5A',
        'drop-bg': '#E7F3EC',
        rise: '#C24429',
        'rise-bg': '#FBEAE6',
        signal: '#B8862E',
        'signal-bg': '#FBF3E4',
        line: '#E4E1D8',
      },
      fontFamily: {
        sans: ['Pretendard', 'system-ui', '-apple-system', 'sans-serif'],
      },
      borderRadius: {
        card: '16px',
      },
    },
  },
  plugins: [],
};
