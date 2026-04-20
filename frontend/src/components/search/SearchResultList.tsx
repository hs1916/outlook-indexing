import type { SearchPage, SearchRequest, SearchResult } from '../../types'

interface Props {
  page: SearchPage | null
  selectedId: number | null
  onSelect: (result: SearchResult) => void
  onPageChange: (page: number) => void
  loading: boolean
  searched: boolean
  lastReq: SearchRequest
}

function formatDate(dt: string | null): string {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

function buildExportUrl(req: SearchRequest): string {
  const p = new URLSearchParams()
  if (req.subject)        p.set('subject',        req.subject)
  if (req.body)           p.set('body',           req.body)
  if (req.senderEmail)    p.set('senderEmail',    req.senderEmail)
  if (req.senderName)     p.set('senderName',     req.senderName)
  if (req.recipientEmail) p.set('recipientEmail', req.recipientEmail)
  if (req.recipientName)  p.set('recipientName',  req.recipientName)
  if (req.dateFrom)       p.set('dateFrom',       req.dateFrom)
  if (req.dateTo)         p.set('dateTo',         req.dateTo)
  if (req.attachment)     p.set('attachment',     req.attachment)
  return `/api/export/mails/text?${p.toString()}`
}

export default function SearchResultList({
  page, selectedId, onSelect, onPageChange, loading, searched, lastReq,
}: Props) {
  const isEmpty = !page || page.content.length === 0

  return (
    <div className="flex flex-col h-full bg-white border-r border-gray-200">

      {/* 헤더: 건수 + TXT 내보내기 */}
      <div className="flex items-center justify-between px-2 py-1.5 border-b border-gray-100 bg-gray-50 flex-shrink-0">
        <span className="text-xs text-gray-500">
          {page && !loading
            ? <><span className="font-semibold text-gray-700">{page.totalElements.toLocaleString()}</span>건</>
            : <span className="text-gray-300">-</span>
          }
        </span>
        {searched && !loading && page && page.totalElements > 0 && (
          <a
            href={buildExportUrl(lastReq)}
            download="mail_export.txt"
            className="px-1.5 py-0.5 text-xs bg-green-600 text-white rounded hover:bg-green-700"
            title="검색된 전체 메일 본문을 TXT로 다운로드"
          >
            TXT↓
          </a>
        )}
      </div>

      {/* 상태 메시지 */}
      {(loading || !searched || isEmpty) && (
        <div className="flex-1 flex items-center justify-center text-xs text-gray-400 text-center leading-relaxed p-4">
          {loading
            ? '검색 중…'
            : !searched
              ? '상단에서 검색 조건을\n입력하고 검색하세요.'
              : '검색 결과가 없습니다.'}
        </div>
      )}

      {/* 결과 목록 */}
      {!loading && searched && !isEmpty && page && (
        <>
          <div className="flex-1 overflow-y-auto min-h-0">
            {page.content.map((r) => (
              <div
                key={r.id}
                onClick={() => onSelect(r)}
                className={`px-2.5 py-2 cursor-pointer border-b border-gray-100 hover:bg-blue-50 transition-colors ${
                  selectedId === r.id
                    ? 'bg-blue-50 border-l-2 border-l-blue-500'
                    : 'border-l-2 border-l-transparent'
                }`}
              >
                {/* 제목 */}
                <p className="truncate text-xs font-medium text-gray-800 leading-snug" title={r.subject ?? ''}>
                  {r.subject || '(제목 없음)'}
                </p>
                {/* 보낸사람 + 날짜 */}
                <div className="flex items-center justify-between mt-0.5 gap-1">
                  <span className="truncate text-xs text-gray-500">{r.senderName || '-'}</span>
                  <span className="text-xs text-gray-400 flex-shrink-0">{formatDate(r.sentDate)}</span>
                </div>
                {/* 첨부 */}
                {r.hasAttachment && (
                  <span className="text-xs text-gray-400">📎</span>
                )}
              </div>
            ))}
          </div>

          {/* 페이지네이션 */}
          {page.totalPages > 1 && (
            <div className="flex-shrink-0 flex justify-center items-center gap-1 py-1.5 border-t border-gray-100 bg-gray-50">
              <PagBtn onClick={() => onPageChange(0)} disabled={page.number === 0}>«</PagBtn>
              <PagBtn onClick={() => onPageChange(page.number - 1)} disabled={page.number === 0}>‹</PagBtn>
              <span className="text-xs text-gray-600 px-1 min-w-[3rem] text-center">
                {page.number + 1} / {page.totalPages}
              </span>
              <PagBtn onClick={() => onPageChange(page.number + 1)} disabled={page.number >= page.totalPages - 1}>›</PagBtn>
              <PagBtn onClick={() => onPageChange(page.totalPages - 1)} disabled={page.number >= page.totalPages - 1}>»</PagBtn>
            </div>
          )}
        </>
      )}
    </div>
  )
}

function PagBtn({ onClick, disabled, children }: {
  onClick: () => void; disabled: boolean; children: React.ReactNode
}) {
  return (
    <button
      onClick={onClick} disabled={disabled}
      className="w-6 h-6 text-xs border border-gray-200 rounded bg-white hover:bg-gray-100 disabled:opacity-30 flex items-center justify-center"
    >
      {children}
    </button>
  )
}
