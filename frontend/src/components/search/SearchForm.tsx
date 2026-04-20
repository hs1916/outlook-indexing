import { useState } from 'react'
import type { SearchRequest } from '../../types'

interface Props {
  onSearch: (req: SearchRequest) => void
  loading: boolean
}

const EMPTY: SearchRequest = {
  subject: '',
  body: '',
  senderEmail: '',
  senderName: '',
  recipientEmail: '',
  recipientName: '',
  dateFrom: '',
  dateTo: '',
  attachment: '',
}

export default function SearchForm({ onSearch, loading }: Props) {
  const [form, setForm] = useState<SearchRequest>(EMPTY)

  const set = (key: keyof SearchRequest, val: string) =>
    setForm((prev) => ({ ...prev, [key]: val }))

  const handleSearch = () => onSearch({ ...form, page: 0 })

  const handleReset = () => {
    setForm(EMPTY)
    onSearch({ page: 0 })
  }

  const inputCls = 'w-full border border-gray-300 rounded px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500'
  const labelCls = 'block text-xs font-medium text-gray-500 mb-0.5'

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4 flex flex-col gap-2.5 h-full overflow-y-auto">
      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">검색 조건</p>

      <div>
        <label className={labelCls}>제목</label>
        <input type="text" className={inputCls} placeholder="제목 키워드"
          value={form.subject ?? ''} onChange={(e) => set('subject', e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
      </div>

      <div>
        <label className={labelCls}>본문</label>
        <input type="text" className={inputCls} placeholder="본문 키워드"
          value={form.body ?? ''} onChange={(e) => set('body', e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
      </div>

      {/* 보낸사람 */}
      <div className="border border-gray-100 rounded p-2 bg-gray-50">
        <p className="text-xs font-semibold text-gray-500 mb-1.5">보낸사람</p>
        <div className="flex flex-col gap-1.5">
          <div>
            <label className={labelCls}>이름</label>
            <input type="text" className={inputCls} placeholder="보낸사람 이름"
              value={form.senderName ?? ''} onChange={(e) => set('senderName', e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
          </div>
          <div>
            <label className={labelCls}>이메일</label>
            <input type="text" className={inputCls} placeholder="보낸사람 이메일"
              value={form.senderEmail ?? ''} onChange={(e) => set('senderEmail', e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
          </div>
        </div>
      </div>

      {/* 받는사람 */}
      <div className="border border-gray-100 rounded p-2 bg-gray-50">
        <p className="text-xs font-semibold text-gray-500 mb-1.5">받는사람</p>
        <div className="flex flex-col gap-1.5">
          <div>
            <label className={labelCls}>이름</label>
            <input type="text" className={inputCls} placeholder="받는사람 이름"
              value={form.recipientName ?? ''} onChange={(e) => set('recipientName', e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
          </div>
          <div>
            <label className={labelCls}>이메일</label>
            <input type="text" className={inputCls} placeholder="받는사람 이메일"
              value={form.recipientEmail ?? ''} onChange={(e) => set('recipientEmail', e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
          </div>
        </div>
      </div>

      <div>
        <label className={labelCls}>날짜 범위</label>
        <input type="date" className={inputCls + ' mb-1'}
          value={form.dateFrom ?? ''} onChange={(e) => set('dateFrom', e.target.value)} />
        <div className="text-center text-gray-400 text-xs mb-1">~</div>
        <input type="date" className={inputCls}
          value={form.dateTo ?? ''} onChange={(e) => set('dateTo', e.target.value)} />
      </div>

      <div>
        <label className={labelCls}>첨부파일명</label>
        <input type="text" className={inputCls} placeholder="첨부파일명 키워드"
          value={form.attachment ?? ''} onChange={(e) => set('attachment', e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()} />
      </div>

      <div className="flex flex-col gap-2 mt-auto pt-2">
        <button onClick={handleSearch} disabled={loading}
          className="w-full py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 font-medium">
          {loading ? '검색 중...' : '검색'}
        </button>
        <button onClick={handleReset}
          className="w-full py-2 text-sm text-gray-600 border border-gray-300 rounded hover:bg-gray-50">
          초기화
        </button>
      </div>
    </div>
  )
}
