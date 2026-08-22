import { Heart, Bell, User } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Header() {
  return (
    <header className="border-b border-line bg-white">
      <div className="mx-auto flex h-[60px] max-w-[1040px] items-center justify-between px-5">
        <Link to="/" className="text-lg font-bold text-trust">
          PickAndSave
        </Link>

        <nav className="flex items-center gap-5">
          <Link to="/my/products" className="flex items-center gap-1.5 text-sm text-ink">
            <Heart size={17} aria-hidden="true" />
            관심 상품
          </Link>
          <Link to="/notifications" className="flex items-center gap-1.5 text-sm text-ink">
            <Bell size={17} aria-hidden="true" />
            알림
          </Link>
          <button className="flex items-center gap-1.5 rounded-[10px] bg-trust px-4 py-2 text-sm font-semibold text-white">
            <User size={16} aria-hidden="true" />
            로그인
          </button>
        </nav>
      </div>
    </header>
  );
}
