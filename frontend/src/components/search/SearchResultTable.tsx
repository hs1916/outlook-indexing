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
  if (!dt) return '-'
  return new Date(dt).toLocaleString('ko-KR')
}

function buildExportUrl(req: SearchRequest): string {
  const params = new URLSearchParams()
  if (req.subject)        params.set('subject',        req.subject)
  if (req.body)           params.set('body',           req.body)
  if (req.senderEmail)    params.set('senderEmail',    req.senderEmail)
  if (req.senderName)     params.set('senderName',     req.senderName)
  if (req.recipientEmail) params.set('recipientEmail', req.recipientEmail)
  if (req.recipientName)  params.set('recipientName',  req.recipientName)
  if (req.dateFrom)       params.set('dateFrom',       req.dateFrom)
  if (req.dateTo)         params.set('dateTo',         req.dateTo)
  if (req.attachment)     params.set('attachment',     req.attachment)
  return `/api/export/mails/text?${params.toString()}`
}

export default function SearchResultTable({
  page, selectedId, onSelect, onPageChange, loading, searched, lastReq,
}: Props) {
  const isEmpty = !page || page.content.length === 0

  return (
    <div className="flex flex-col h-full">

      {/* 결과 수 + 내보내기 버튼 */}
      <div className="flex-shrink-0 flex items-center justify-between px-3 pt-3 pb-1">
        <div>
          {page && !loading && (
            <p className="text-xs text-gray-500">
              총 <span className="font-semibold text-gray-700">{page.totalElements.toLocaleString()}</span>건
            </p>
          )}
        </div>
        {searched && !loading && page && page.totalElements > 0 && (
          <a
            href={buildExportUrl(lastReq)}
            download="mail_export.txt"
            className="px-2.5 py-1 text-xs bg-green-600 text-white rounded hover:bg-green-700"
          >
            본문 전체 TXT 다운로드
          </a>
        )}
      </div>

      {/* 상태 메시지 */}
      {(loading || !searched || isEmpty) && (
        <div className="flex-1 flex items-center justify-center text-sm text-gray-400">
          {loading ? '검색 중...' : !searched ? '검색 조건을 입력하고 검색 버튼을 누르세요.' : '검색 결과가 없습니다.'}
        </div>
      )}

      {/* 테이블 (높이를 꽉 채움) */}
      {!loading && searched && !isEmpty && page && (
        <>
          <div className="flex-1 overflow-hidden flex flex-col min-h-0 mx-3">
            {/* 고정 헤더 */}
            <table className="w-full text-sm table-fixed flex-shrink-0">
              <colgroup>
                <col style={{ width: '45%' }} />
                <col style={{ width: '25%' }} />
                <col style={{ width: '25%' }} />
                <col style={{ width: '5%' }} />
              </colgroup>
              <thead>
                <tr className="bg-gray-50 border border-gray-200 rounded-t-lg text-xs text-gray-500">
                  <th className="px-3 py-2 text-left rounded-tl-lg">제목</th>
                  <th className="px-3 py-2 text-left">보낸사람</th>
                  <th className="px-3 py-2 text-left">날짜</th>
                  <th className="px-3 py-2 text-center rounded-tr-lg">첨부</th>
                </tr>
              </thead>
            </table>

            {/* 스크롤 tbody */}
            <div className="flex-1 overflow-y-auto border-x border-b border-gray-200 rounded-b-lg">
              <table className="w-full text-sm table-fixed">
                <colgroup>
                  <col style={{ width: '45%' }} />
                  <col style={{ width: '25%' }} />
                  <col style={{ width: '25%' }} />
                  <col style={{ width: '5%' }} />
                </colgroup>
                <tbody className="divide-y divide-gray-100">
                  {page.content.map((r) => (
                    <tr key={r.id} onClick={() => onSelect(r)}
                      className={`cursor-pointer hover:bg-blue-50 transition-colors ${
                        selectedId === r.id ? 'bg-blue-50 border-l-2 border-l-blue-500' : ''
                      }`}>
                      <td className="px-3 py-2.5">
                        <span className="block truncate font-medium text-gray-800" title={r.subject ?? ''}>
                          {r.subject || '(제목 없음)'}
                        </span>
                      </td>
                      <td className="px-3 py-2.5">
                        <span className="block truncate text-gray-700" title={r.senderName ?? ''}>
                          {r.senderName || '-'}
                        </span>
                      </td>
                      <td className="px-3 py-2.5 text-gray-400 text-xs whitespace-nowrap">
                        {formatDate(r.sentDate)}
                      </td>
                      <td className="px-3 py-2.5 text-center">
                        {r.hasAttachment
                          ? <span title="첨부파일 있음">📎</span>
                          : <span className="text-gray-200">-</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* 페이지네이션 */}
          {page.totalPages > 1 && (
            <div className="flex-shrink-0 flex justify-center items-center gap-2 py-2 px-3">
              <button onClick={() => onPageChange(0)} disabled={page.number === 0}
                className="px-2 py-1 text-xs border rounded disabled:opacity-40">처음</button>
              <button onClick={() => onPageChange(page.number - 1)} disabled={page.number === 0}
                className="px-2 py-1 text-xs border rounded disabled:opacity-40">이전</button>
              <span className="text-xs text-gray-600">{page.number + 1} / {page.totalPages}</span>
              <button onClick={() => onPageChange(page.number + 1)} disabled={page.number >= page.totalPages - 1}
                className="px-2 py-1 text-xs border rounded disabled:opacity-40">다음</button>
              <button onClick={() => onPageChange(page.totalPages - 1)} disabled={page.number >= page.totalPages - 1}
                className="px-2 py-1 text-xs border rounded disabled:opacity-40">마지막</button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
