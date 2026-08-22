import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      {/* 추후 추가 예정: /products/:id, /my/products, /oauth2/redirect */}
    </Routes>
  );
}
