import { useEffect, useState } from 'react'
import { getMailDetail } from '../../api/searchApi'
import type { MailDetail } from '../../types'

interface Props {
  mailId: number | null
  onClose: () => void
}

function formatDate(dt: string | null): string {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('ko-KR')
}

/** 콤마 구분 이름/이메일 배열을 [{name, email}] 형태로 합치기 */
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

export default function MailDetailPanel({ mailId, onClose }: Props) {
  const [detail, setDetail] = useState<MailDetail | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!mailId) { setDetail(null); return }
    setLoading(true)
    getMailDetail(mailId).then(setDetail).finally(() => setLoading(false))
  }, [mailId])

  if (!mailId) return null

  const recipients = detail ? zipRecipients(detail.recipientNames, detail.recipientEmails) : []

  return (
    <div className="border border-gray-200 rounded-lg bg-white h-full flex flex-col">
      <div className="flex items-center justify-between px-4 py-2.5 border-b border-gray-100 flex-shrink-0">
        <h3 className="font-semibold text-gray-700 text-sm">메일 상세</h3>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-lg leading-none">×</button>
      </div>

      {loading && (
        <div className="flex-1 flex items-center justify-center text-gray-400 text-sm">불러오는 중...</div>
      )}

      {!loading && detail && (
        <div className="flex-1 overflow-y-auto p-4 space-y-3 text-sm">

          {/* 제목 */}
          <div>
            <p className="text-xs text-gray-400 mb-0.5">제목</p>
            <p className="font-semibold text-gray-800">{detail.subject || '(제목 없음)'}</p>
          </div>

          {/* 보낸사람 */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <p className="text-xs text-gray-400 mb-0.5">보낸사람</p>
              {detail.senderName && <p className="text-gray-800">{detail.senderName}</p>}
              {detail.senderEmail && <p className="text-gray-500 text-xs">{detail.senderEmail}</p>}
              {!detail.senderName && !detail.senderEmail && <p className="text-gray-400">-</p>}
            </div>
            <div>
              <p className="text-xs text-gray-400 mb-0.5">날짜</p>
              <p className="text-gray-700">{formatDate(detail.sentDate)}</p>
            </div>
          </div>

          {/* 받는사람 */}
          <div>
            <p className="text-xs text-gray-400 mb-1">받는사람 ({recipients.length}명)</p>
            {recipients.length === 0 ? (
              <p className="text-gray-400">-</p>
            ) : (
              <ul className="space-y-1">
                {recipients.map((r, i) => (
                  <li key={i} className="flex items-baseline gap-1.5">
                    {r.name && <span className="text-gray-800">{r.name}</span>}
                    {r.email && (
                      <span className="text-gray-400 text-xs">{r.name ? `<${r.email}>` : r.email}</span>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* 첨부파일 */}
          {detail.attachmentNames.length > 0 && (
            <div>
              <p className="text-xs text-gray-400 mb-0.5">첨부파일</p>
              <ul className="space-y-1">
                {detail.attachmentNames.map((name, i) => (
                  <li key={i} className="flex items-center gap-2">
                    <span className="text-gray-400">📎</span>
                    <span className="flex-1 text-sm text-gray-700 truncate" title={name}>{name}</span>
                    <a
                      href={`/api/mails/${detail.id}/attachments/${i}`}
                      download={name}
                      className="flex-shrink-0 px-2 py-0.5 text-xs bg-blue-50 text-blue-600 border border-blue-200 rounded hover:bg-blue-100"
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

          {/* 본문 */}
          <div>
            <p className="text-xs text-gray-400 mb-1">본문</p>
            <div className="bg-gray-50 rounded p-3 text-sm text-gray-700 whitespace-pre-wrap break-words max-h-60 overflow-y-auto">
              {detail.body || '(본문 없음)'}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
