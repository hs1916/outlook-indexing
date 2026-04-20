import { useEffect, useRef, useState } from 'react'
import { getMailDetail } from '../../api/searchApi'
import type { MailDetail } from '../../types'

interface Props {
  mailId: number
  onClose: () => void
}

function formatDate(dt: string | null): string {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('ko-KR')
}

function zipRecipients(names: string | null, emails: string | null) {
  const nameArr = (names ?? '').split(',').map((s) => s.trim())
  const emailArr = (emails ?? '').split(',').map((s) => s.trim())
  const len = Math.max(nameArr.length, emailArr.length)
  const result = []
  for (let i = 0; i < len; i++) {
    const name = nameArr[i] || ''
    const email = emailArr[i] || ''
    if (name || email) result.push({ name, email })
  }
  return result
}

export default function MailDetailModal({ mailId, onClose }: Props) {
  const [detail, setDetail] = useState<MailDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const overlayRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setLoading(true)
    setDetail(null)
    getMailDetail(mailId).then(setDetail).finally(() => setLoading(false))
  }, [mailId])

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose() }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === overlayRef.current) onClose()
  }

  const recipients = detail ? zipRecipients(detail.recipientNames, detail.recipientEmails) : []

  return (
    <div
      ref={overlayRef}
      onClick={handleOverlayClick}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
    >
      {/* 1.3배 확대: max-w-2xl(42rem) → ~54rem, 80vh → 90vh */}
      <div
        className="bg-white rounded-lg shadow-2xl w-full mx-4 flex flex-col"
        style={{ maxWidth: '54rem', maxHeight: '90vh' }}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 py-3.5 border-b border-gray-200 flex-shrink-0">
          <h2 className="font-semibold text-gray-800">메일 상세</h2>
          <button onClick={onClose}
            className="text-gray-400 hover:text-gray-700 text-2xl leading-none px-1">×</button>
        </div>

        {/* 내용 */}
        {loading ? (
          <div className="flex-1 flex items-center justify-center text-gray-400 py-16">
            불러오는 중...
          </div>
        ) : detail ? (
          <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4 text-sm min-h-0">

            {/* 제목 */}
            <div>
              <p className="text-xs text-gray-400 mb-1">제목</p>
              <p className="font-semibold text-gray-900 text-base leading-snug">
                {detail.subject || '(제목 없음)'}
              </p>
            </div>

            {/* 메타 정보 */}
            <div className="grid grid-cols-2 gap-4 bg-gray-50 rounded-lg p-4">
              <div>
                <p className="text-xs text-gray-400 mb-0.5">보낸사람</p>
                {detail.senderName && <p className="text-gray-800 font-medium">{detail.senderName}</p>}
                {detail.senderEmail && <p className="text-gray-500 text-xs">{detail.senderEmail}</p>}
                {!detail.senderName && !detail.senderEmail && <p className="text-gray-400">-</p>}
              </div>
              <div>
                <p className="text-xs text-gray-400 mb-0.5">날짜</p>
                <p className="text-gray-800">{formatDate(detail.sentDate)}</p>
              </div>
            </div>

            {/* 받는사람 */}
            <div>
              <p className="text-xs text-gray-400 mb-1">받는사람 ({recipients.length}명)</p>
              {recipients.length === 0 ? (
                <p className="text-gray-400">-</p>
              ) : (
                <div className="space-y-0.5">
                  {recipients.map((r, i) => (
                    <div key={i} className="flex items-baseline gap-1.5">
                      {r.name && <span className="text-gray-800">{r.name}</span>}
                      {r.email && (
                        <span className="text-gray-400 text-xs">
                          {r.name ? `<${r.email}>` : r.email}
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* 첨부파일 */}
            {detail.attachmentNames.length > 0 && (
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <p className="text-xs text-gray-400">첨부파일 ({detail.attachmentNames.length}개)</p>
                  {detail.attachmentNames.length > 1 && (
                    <a
                      href={`/api/mails/${detail.id}/attachments/zip`}
                      download={`attachments_${detail.id}.zip`}
                      className="px-2 py-0.5 text-xs bg-indigo-600 text-white rounded hover:bg-indigo-700"
                    >
                      전체 ZIP 다운로드
                    </a>
                  )}
                </div>
                <ul className="space-y-1">
                  {detail.attachmentNames.map((name, i) => (
                    <li key={i} className="flex items-center gap-2 bg-gray-50 rounded px-3 py-1.5">
                      <span className="text-gray-400 text-xs">📎</span>
                      <span className="flex-1 text-gray-700 truncate" title={name}>{name}</span>
                      <a
                        href={`/api/mails/${detail.id}/attachments/${i}`}
                        download={name}
                        className="flex-shrink-0 px-2.5 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700"
                      >
                        다운로드
                      </a>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* PST 파일 */}
            <div>
              <p className="text-xs text-gray-400 mb-0.5">PST 파일</p>
              <p className="text-xs text-gray-500">{detail.pstFileName}</p>
            </div>

            {/* 본문 — HTML 렌더링 (iframe) */}
            <div className="flex flex-col" style={{ minHeight: '320px' }}>
              <p className="text-xs text-gray-400 mb-1 flex-shrink-0">본문</p>
              <iframe
                key={mailId}
                src={`/api/mails/${mailId}/html-body`}
                className="flex-1 w-full border border-gray-200 rounded-lg bg-white"
                style={{ minHeight: '320px' }}
                sandbox="allow-same-origin"
                title="메일 본문"
              />
            </div>
          </div>
        ) : null}

        {/* 푸터 */}
        <div className="flex justify-end px-6 py-3 border-t border-gray-100 flex-shrink-0">
          <button onClick={onClose}
            className="px-4 py-1.5 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200">
            닫기
          </button>
        </div>
      </div>
    </div>
  )
}
