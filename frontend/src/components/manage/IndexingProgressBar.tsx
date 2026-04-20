import { useEffect, useRef, useState } from 'react'
import { startIndexing, subscribeProgress } from '../../api/pstApi'
import type { IndexProgress } from '../../types'

interface Props {
  pstFileId: number
  onComplete: () => void
}

export default function IndexingProgressBar({ pstFileId, onComplete }: Props) {
  const [progress, setProgress] = useState<IndexProgress>({
    indexedCount: 0,
    totalCount: 0,
    percent: 0,
    status: 'INDEXING',
  })
  const startedRef = useRef(false)

  useEffect(() => {
    if (startedRef.current) return
    startedRef.current = true

    const run = async () => {
      try {
        await startIndexing(pstFileId)
        subscribeProgress(
          pstFileId,
          (p) => setProgress(p),
          () => onComplete(),
          () => onComplete(),
        )
      } catch {
        setProgress((prev) => ({ ...prev, status: 'ERROR' }))
        onComplete()
      }
    }

    run()
  }, [pstFileId, onComplete])

  return (
    <div className="w-full">
      <div className="flex items-center justify-between text-xs text-gray-600 mb-1">
        <span>
          {progress?.indexedCount?.toLocaleString() ?? 0} /{' '}
          {progress?.totalCount?.toLocaleString() ?? 0} 건
        </span>
        <span>{progress?.percent ?? 0}%</span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div
          className={`h-2 rounded-full transition-all duration-300 ${
            progress?.status === 'ERROR' ? 'bg-red-500' : 'bg-blue-500'
          }`}
          style={{ width: `${progress?.percent ?? 0}%` }}
        />
      </div>
      {progress?.status === 'DONE' && (
        <p className="text-xs text-green-600 mt-1">
          완료 — {progress.elapsedSeconds}초 소요
          {progress.errorCount ? ` (오류 ${progress.errorCount}건)` : ''}
        </p>
      )}
      {progress?.status === 'ERROR' && (
        <p className="text-xs text-red-500 mt-1">인덱싱 중 오류 발생</p>
      )}
    </div>
  )
}
