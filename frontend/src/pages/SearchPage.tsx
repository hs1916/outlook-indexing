import { useCallback, useRef, useState } from 'react'
import { searchMails } from '../api/searchApi'
import MailContentPanel from '../components/search/MailContentPanel'
import SearchResultList from '../components/search/SearchResultList'
import SearchRibbon from '../components/search/SearchRibbon'
import type { SearchPage, SearchRequest, SearchResult } from '../types'

const DEFAULT_LEFT_PERCENT = 25
const MIN_LEFT_PERCENT = 15
const MAX_LEFT_PERCENT = 60

export default function SearchPage() {
  const [results, setResults] = useState<SearchPage | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)
  const [lastReq, setLastReq] = useState<SearchRequest>({})
  const [error, setError] = useState('')
  const [ribbonOpen, setRibbonOpen] = useState(true)
  const [leftPercent, setLeftPercent] = useState(DEFAULT_LEFT_PERCENT)

  const containerRef = useRef<HTMLDivElement>(null)
  const isDragging = useRef(false)

  const handleSearch = async (req: SearchRequest) => {
    setLoading(true)
    setError('')
    setLastReq(req)
    try {
      const data = await searchMails({ ...req, page: 0 })
      setResults(data)
      setSearched(true)
      setSelectedId(null)
    } catch {
      setError('검색 중 오류가 발생했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handlePageChange = async (page: number) => {
    try {
      const data = await searchMails({ ...lastReq, page })
      setResults(data)
      setSelectedId(null)
    } catch {
      setError('페이지 이동 중 오류가 발생했습니다.')
    }
  }

  const handleSelect = (r: SearchResult) => {
    setSelectedId(r.id)
  }

  // 드래그 구분선 핸들러
  const startDrag = useCallback((e: React.MouseEvent) => {
    e.preventDefault()
    isDragging.current = true

    const onMove = (ev: MouseEvent) => {
      if (!isDragging.current || !containerRef.current) return
      const rect = containerRef.current.getBoundingClientRect()
      const pct = ((ev.clientX - rect.left) / rect.width) * 100
      setLeftPercent(Math.min(Math.max(pct, MIN_LEFT_PERCENT), MAX_LEFT_PERCENT))
    }

    const onUp = () => {
      isDragging.current = false
      document.removeEventListener('mousemove', onMove)
      document.removeEventListener('mouseup', onUp)
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
    }

    document.addEventListener('mousemove', onMove)
    document.addEventListener('mouseup', onUp)
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'
  }, [])

  return (
    <div className="flex flex-col h-full min-h-0 overflow-hidden">

      {/* 리본 검색 조건 */}
      <SearchRibbon
        open={ribbonOpen}
        onToggle={() => setRibbonOpen(o => !o)}
        onSearch={handleSearch}
        loading={loading}
      />

      {/* 오류 메시지 */}
      {error && (
        <div className="flex-shrink-0 px-3 py-1.5 bg-red-50 border-b border-red-200 text-red-600 text-xs">
          {error}
        </div>
      )}

      {/* 메인 패널 영역 */}
      <div ref={containerRef} className="flex flex-1 min-h-0 overflow-hidden">

        {/* 좌측: 검색 결과 목록 */}
        <div
          className="flex flex-col min-h-0 min-w-0 overflow-hidden"
          style={{ width: `${leftPercent}%` }}
        >
          <SearchResultList
            page={results}
            selectedId={selectedId}
            onSelect={handleSelect}
            onPageChange={handlePageChange}
            loading={loading}
            searched={searched}
            lastReq={lastReq}
          />
        </div>

        {/* 드래그 구분선 */}
        <div
          onMouseDown={startDrag}
          className="flex-shrink-0 w-1 bg-gray-200 hover:bg-blue-400 active:bg-blue-500 cursor-col-resize transition-colors"
          title="드래그하여 패널 크기 조절"
        />

        {/* 우측: 메일 내용 */}
        <div className="flex-1 min-h-0 min-w-0 overflow-hidden">
          <MailContentPanel mailId={selectedId} />
        </div>
      </div>
    </div>
  )
}
