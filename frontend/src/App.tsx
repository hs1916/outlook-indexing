import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import ManagePage from './pages/ManagePage'
import SearchPage from './pages/SearchPage'

export default function App() {
  const { pathname } = useLocation()
  const isSearch = pathname.startsWith('/search')

  return (
    <div className="h-screen flex flex-col overflow-hidden bg-gray-50">
      {/* 헤더 */}
      <header className="flex-shrink-0 bg-white border-b border-gray-200 shadow-sm">
        <div className="px-4 h-11 flex items-center justify-between">
          <span className="font-bold text-gray-800">📧 PST 메일 검색</span>
          <nav className="flex gap-1">
            <Link
              to="/manage"
              className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                pathname.startsWith('/manage')
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              PST 관리
            </Link>
            <Link
              to="/search"
              className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                isSearch
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              메일 검색
            </Link>
          </nav>
        </div>
      </header>

      {/* 본문 — 검색 페이지는 full-width/height, 관리 페이지는 기존 레이아웃 */}
      {isSearch ? (
        <main className="flex-1 min-h-0 overflow-hidden flex flex-col">
          <Routes>
            <Route path="/search" element={<SearchPage />} />
          </Routes>
        </main>
      ) : (
        <main className="flex-1 min-h-0 overflow-auto">
          <div className="max-w-5xl mx-auto px-6 py-6">
            <Routes>
              <Route path="/" element={<Navigate to="/manage" replace />} />
              <Route path="/manage" element={<ManagePage />} />
            </Routes>
          </div>
        </main>
      )}
    </div>
  )
}
