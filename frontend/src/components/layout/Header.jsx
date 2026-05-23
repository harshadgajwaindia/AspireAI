import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import { LogOut, Menu, X } from "lucide-react";

const NAV = [
  { to: "/", label: "Analyzer" },
  { to: "/roadmap", label: "Roadmap" },
  { to: "/interview", label: "Mock Interview" },
  { to: "/dashboard", label: "Dashboard" },
  { to: "/jobs/recommended", label: "Jobs" },
];

export function Header() {
  const logout = useAuthStore((state) => state.logout);
  const user = useAuthStore((state) => state.user);
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="sticky top-0 z-50 backdrop-blur-md bg-[#0a0a0c]/80 border-b border-white/5">
      <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        {/* Logo */}
        <NavLink to="/" className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-brand-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-brand-500/25">
            <svg viewBox="0 0 24 24" className="w-4 h-4" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"
                    stroke="white" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span className="font-display font-bold text-white tracking-tight text-lg">
            Aspire<span className="text-brand-500">AI</span>
          </span>
        </NavLink>

        {/* Desktop Nav */}
        <nav className="hidden md:flex items-center gap-1">
          {NAV.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-350 font-body
                 ${isActive
                   ? "bg-brand-600 text-white shadow-lg shadow-brand-600/10"
                   : "text-slate-400 hover:text-white hover:bg-white/5"
                 }`
              }
            >
              {label}
            </NavLink>
          ))}
        </nav>

        {/* User Stats & Logout */}
        <div className="hidden md:flex items-center gap-4">
          <div className="flex items-center gap-3 pr-4 border-r border-white/5">
            <div className="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-500 to-purple-700
                       flex items-center justify-center text-white text-sm font-bold font-display uppercase shadow-md">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div>
              <p className="text-xs font-semibold text-slate-200 truncate max-w-[100px]">{user?.fullName}</p>
              <p className="text-[10px] text-slate-400">Standard User</p>
            </div>
          </div>
          
          <button
            onClick={handleLogout}
            className="p-2 text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 rounded-lg transition-all"
            title="Logout"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>

        {/* Mobile menu trigger */}
        <div className="flex md:hidden items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-700
                     flex items-center justify-center text-white text-xs font-bold font-display uppercase shadow-md">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="p-2 text-slate-400 hover:text-white hover:bg-white/5 rounded-lg transition-all"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-white/5 bg-[#0a0a0c] px-6 py-4 space-y-4 animate-fade-in">
          <nav className="flex flex-col gap-1.5">
            {NAV.map(({ to, label }) => (
              <NavLink
                key={to}
                to={to}
                onClick={() => setMobileMenuOpen(false)}
                className={({ isActive }) =>
                  `px-4 py-2.5 rounded-xl text-sm font-semibold transition-all font-body block
                   ${isActive
                     ? "bg-brand-600 text-white shadow-lg"
                     : "text-slate-400 hover:text-white hover:bg-white/5"
                   }`
                }
              >
                {label}
              </NavLink>
            ))}
          </nav>
          
          <div className="pt-4 border-t border-white/5 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-700
                         flex items-center justify-center text-white text-xs font-bold font-display uppercase">
                {user?.fullName?.charAt(0) || 'U'}
              </div>
              <div>
                <p className="text-xs font-semibold text-slate-200">{user?.fullName}</p>
                <p className="text-[10px] text-slate-400">Standard User</p>
              </div>
            </div>
            <button
              onClick={() => {
                setMobileMenuOpen(false);
                handleLogout();
              }}
              className="p-2.5 text-slate-400 hover:text-rose-500 hover:bg-rose-500/10 rounded-lg transition-all flex items-center gap-2 text-xs font-body font-semibold"
            >
              <LogOut className="w-4 h-4" />
              Sign Out
            </button>
          </div>
        </div>
      )}
    </header>
  );
}