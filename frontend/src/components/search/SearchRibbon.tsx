import { useState } from 'react'
import type { SearchRequest } from '../../types'

interface Props {
  open: boolean
  onToggle: () => void
  onSearch: (req: SearchRequest) => void
  loading: boolean
}

export default function SearchRibbon({ open, onToggle, onSearch, loading }: Props) {
  const [form, setForm] = useState<SearchRequest>({})

  const set = (key: keyof SearchRequest) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm(f => ({ ...f, [key]: e.target.value || undefined }))

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSearch({ ...form, page: 0 })
  }

  const handleReset = () => {
    setForm({})
    onSearch({ page: 0 })
  }

  return (
    <div className="flex-shrink-0 bg-gray-50 border-b border-gray-200 select-none">
      {/* 토글 헤더 */}
      <button
        type="button"
        onClick={onToggle}
        className="w-full flex items-center gap-2 px-3 py-1.5 hover:bg-gray-100 transition-colors text-left"
      >
        <span className="text-xs font-semibold text-gray-600 tracking-wide">검색 조건</span>
        <svg
          className={`w-3 h-3 text-gray-400 transition-transform duration-200 ${open ? '' : '-rotate-180'}`}
          viewBox="0 0 10 6" fill="none" stroke="currentColor" strokeWidth="1.5"
        >
          <path d="M1 5L5 1L9 5" />
        </svg>
        {!open && (
          <span className="text-xs text-gray-400 ml-1">
            (클릭하여 펼치기)
          </span>
        )}
      </button>

      {/* 리본 본체 */}
      {open && (
        <form onSubmit={handleSubmit} className="px-3 pb-3 pt-1">
          <div className="flex flex-wrap gap-x-4 gap-y-2 items-end">

            <Field label="제목" value={form.subject} onChange={set('subject')} width="w-32" />
            <Field label="본문" value={form.body} onChange={set('body')} width="w-32" />

            {/* 보낸사람 그룹 */}
            <div className="flex gap-1 items-end">
              <div className="flex flex-col gap-0.5">
                <span className="text-xs text-gray-500 font-medium">보낸사람</span>
                <div className="flex gap-1">
                  <Field label="이름" value={form.senderName} onChange={set('senderName')} width="w-24" noTopLabel />
                  <Field label="이메일" value={form.senderEmail} onChange={set('senderEmail')} width="w-28" noTopLabel />
                </div>
              </div>
            </div>

            {/* 받는사람 그룹 */}
            <div className="flex gap-1 items-end">
              <div className="flex flex-col gap-0.5">
                <span className="text-xs text-gray-500 font-medium">받는사람</span>
                <div className="flex gap-1">
                  <Field label="이름" value={form.recipientName} onChange={set('recipientName')} width="w-24" noTopLabel />
                  <Field label="이메일" value={form.recipientEmail} onChange={set('recipientEmail')} width="w-28" noTopLabel />
                </div>
              </div>
            </div>

            {/* 날짜 범위 */}
            <div className="flex flex-col gap-0.5">
              <span className="text-xs text-gray-500 font-medium">날짜 범위</span>
              <div className="flex items-center gap-1">
                <input
                  type="date" value={form.dateFrom ?? ''}
                  onChange={set('dateFrom')}
                  className="border border-gray-300 rounded px-1.5 py-1 text-xs bg-white focus:outline-none focus:border-blue-400 w-32"
                />
                <span className="text-gray-400 text-xs flex-shrink-0">~</span>
                <input
                  type="date" value={form.dateTo ?? ''}
                  onChange={set('dateTo')}
                  className="border border-gray-300 rounded px-1.5 py-1 text-xs bg-white focus:outline-none focus:border-blue-400 w-32"
                />
              </div>
            </div>

            <Field label="첨부파일명" value={form.attachment} onChange={set('attachment')} width="w-28" />

            {/* 버튼 */}
            <div className="flex items-end gap-1.5 ml-auto">
              <button
                type="button" onClick={handleReset}
                className="px-3 py-1 text-xs border border-gray-300 rounded bg-white hover:bg-gray-100 text-gray-600 transition-colors"
              >
                초기화
              </button>
              <button
                type="submit" disabled={loading}
                className="px-4 py-1 text-xs bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 font-medium transition-colors"
              >
                {loading ? '검색 중…' : '검색'}
              </button>
            </div>
          </div>
        </form>
      )}
    </div>
  )
}

interface FieldProps {
  label: string
  value: string | undefined
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  width: string
  noTopLabel?: boolean
}

function Field({ label, value, onChange, width, noTopLabel }: FieldProps) {
  return (
    <div className="flex flex-col gap-0.5">
      {!noTopLabel && <span className="text-xs text-gray-500 font-medium">{label}</span>}
      <input
        type="text"
        value={value ?? ''}
        onChange={onChange}
        placeholder={label}
        className={`border border-gray-300 rounded px-2 py-1 text-xs bg-white focus:outline-none focus:border-blue-400 ${width}`}
      />
    </div>
  )
}
