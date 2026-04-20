import { useState } from 'react'
import type { PstFile } from '../../types'
import IndexingProgressBar from './IndexingProgressBar'

interface Props {
  files: PstFile[]
  onDelete: (id: number) => void
  onRefresh: () => void
}

const STATUS_LABEL: Record<string, { text: string; cls: string }> = {
  PENDING: { text: '미인덱싱', cls: 'bg-gray-100 text-gray-600' },
  INDEXING: { text: '진행중', cls: 'bg-blue-100 text-blue-700' },
  DONE: { text: '완료', cls: 'bg-green-100 text-green-700' },
  ERROR: { text: '오류', cls: 'bg-red-100 text-red-700' },
}

function formatBytes(bytes: number): string {
  if (!bytes) return '-'
  if (bytes >= 1024 ** 3) return `${(bytes / 1024 ** 3).toFixed(1)} GB`
  if (bytes >= 1024 ** 2) return `${(bytes / 1024 ** 2).toFixed(1)} MB`
  return `${(bytes / 1024).toFixed(1)} KB`
}

function formatDate(dt: string | null): string {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('ko-KR')
}

export default function PstFileTable({ files, onDelete, onRefresh }: Props) {
  const [indexingIds, setIndexingIds] = useState<Set<number>>(new Set())

  const handleIndexStart = (id: number) => {
    setIndexingIds((prev) => new Set(prev).add(id))
  }

  const handleIndexComplete = (id: number) => {
    setIndexingIds((prev) => {
      const next = new Set(prev)
      next.delete(id)
      return next
    })
    onRefresh()
  }

  if (files.length === 0) {
    return (
      <div className="text-center py-16 text-gray-400">
        등록된 PST 파일이 없습니다. 파일을 추가해주세요.
      </div>
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm text-left">
        <thead className="bg-gray-50 text-gray-600 text-xs uppercase">
          <tr>
            <th className="px-4 py-3">파일명</th>
            <th className="px-4 py-3">크기</th>
            <th className="px-4 py-3">상태</th>
            <th className="px-4 py-3">메일 수</th>
            <th className="px-4 py-3">마지막 인덱싱</th>
            <th className="px-4 py-3">인덱싱</th>
            <th className="px-4 py-3">삭제</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {files.map((f) => {
            const statusInfo = STATUS_LABEL[f.status] ?? STATUS_LABEL.PENDING
            const isIndexing = indexingIds.has(f.id) || f.status === 'INDEXING'

            return (
              <tr key={f.id} className="hover:bg-gray-50">
                <td className="px-4 py-3">
                  <p className="font-medium text-gray-800">{f.fileName}</p>
                  <p className="text-xs text-gray-400 truncate max-w-xs" title={f.filePath}>
                    {f.filePath}
                  </p>
                </td>
                <td className="px-4 py-3 text-gray-600">{formatBytes(f.fileSizeBytes)}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusInfo.cls}`}>
                    {statusInfo.text}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-600">
                  {f.status === 'DONE'
                    ? `${f.indexedMailCount?.toLocaleString()} 건`
                    : '-'}
                </td>
                <td className="px-4 py-3 text-gray-500 text-xs">
                  {formatDate(f.indexedAt)}
                </td>
                <td className="px-4 py-3 min-w-[200px]">
                  {isIndexing ? (
                    <IndexingProgressBar
                      pstFileId={f.id}
                      onComplete={() => handleIndexComplete(f.id)}
                    />
                  ) : (
                    <button
                      onClick={() => handleIndexStart(f.id)}
                      className="px-3 py-1 text-xs bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                      {f.status === 'DONE' ? '재인덱싱' : '인덱싱 시작'}
                    </button>
                  )}
                </td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => {
                      if (confirm(`"${f.fileName}"을 삭제하시겠습니까?\n인덱스 데이터도 함께 삭제됩니다.`)) {
                        onDelete(f.id)
                      }
                    }}
                    className="px-3 py-1 text-xs bg-red-50 text-red-600 rounded hover:bg-red-100"
                  >
                    삭제
                  </button>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
