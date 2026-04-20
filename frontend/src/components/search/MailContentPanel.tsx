import { useEffect, useState } from 'react'
import { getMailDetail } from '../../api/searchApi'
import type { MailDetail } from '../../types'

interface Props {
  mailId: number | null
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

export default function MailContentPanel({ mailId }: Props) {
  const [detail, setDetail] = useState<MailDetail | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!mailId) { setDetail(null); return }
    setLoading(true)
    setDetail(null)
    getMailDetail(mailId).then(setDetail).finally(() => setLoading(false))
  }, [mailId])

  if (!mailId) {
    return (
      <div className="h-full flex items-center justify-center bg-gray-50">
        <p className="text-sm text-gray-300 select-none">← 목록에서 메일을 클릭하면 내용이 표시됩니다</p>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="h-full flex items-center justify-center bg-white">
        <p className="text-sm text-gray-400">불러오는 중…</p>
      </div>
    )
  }

  if (!detail) return null

  const recipients = zipRecipients(detail.recipientNames, detail.recipientEmails)

  return (
    <div className="flex flex-col h-full bg-white overflow-hidden">

      {/* 메타 정보 헤더 (고정) */}
      <div className="flex-shrink-0 border-b border-gray-200 px-4 py-3 space-y-2 overflow-x-auto">

        {/* 제목 */}
        <h2 className="font-semibold text-gray-900 text-sm leading-snug">
          {detail.subject || '(제목 없음)'}
        </h2>

        {/* 보낸사람 + 날짜 */}
        <div className="flex flex-wrap gap-x-6 gap-y-1 text-xs">
          <div>
            <span className="text-gray-400">보낸사람 </span>
            <span className="text-gray-800 font-medium">{detail.senderName || '-'}</span>
            {detail.senderEmail && (
              <span className="text-gray-400"> &lt;{detail.senderEmail}&gt;</span>
            )}
          </div>
          <div>
            <span className="text-gray-400">날짜 </span>
            <span className="text-gray-700">{formatDate(detail.sentDate)}</span>
          </div>
        </div>

        {/* 받는사람 */}
        {recipients.length > 0 && (
          <div className="text-xs">
            <span className="text-gray-400">받는사람 </span>
            {recipients.slice(0, 5).map((r, i) => (
              <span key={i} className="text-gray-700">
                {i > 0 && <span className="text-gray-300">, </span>}
                {r.name || r.email}
                {r.name && r.email && <span className="text-gray-400"> &lt;{r.email}&gt;</span>}
              </span>
            ))}
            {recipients.length > 5 && (
              <span className="text-gray-400"> 외 {recipients.length - 5}명</span>
            )}
          </div>
        )}

        {/* 첨부파일 */}
        {detail.attachmentNames.length > 0 && (
          <div className="flex flex-wrap items-center gap-1.5 text-xs">
            <span className="text-gray-400">첨부 </span>
            {detail.attachmentNames.map((name, i) => (
              <a
                key={i}
                href={`/api/mails/${detail.id}/attachments/${i}`}
                download={name}
                className="flex items-center gap-1 px-2 py-0.5 bg-gray-100 rounded hover:bg-gray-200 text-gray-700 max-w-[12rem]"
                title={name}
              >
                <span className="text-gray-400">📎</span>
                <span className="truncate">{name}</span>
              </a>
            ))}
            {detail.attachmentNames.length > 1 && (
              <a
                href={`/api/mails/${detail.id}/attachments/zip`}
                download={`attachments_${detail.id}.zip`}
                className="px-2 py-0.5 bg-indigo-50 border border-indigo-200 text-indigo-600 rounded hover:bg-indigo-100 text-xs"
              >
                전체 ZIP
              </a>
            )}
          </div>
        )}
      </div>

      {/* HTML 본문 (나머지 영역 전체) */}
      <iframe
        key={mailId}
        src={`/api/mails/${mailId}/html-body`}
        className="flex-1 w-full border-0 min-h-0"
        sandbox="allow-same-origin"
        title="메일 본문"
      />
    </div>
  )
}
