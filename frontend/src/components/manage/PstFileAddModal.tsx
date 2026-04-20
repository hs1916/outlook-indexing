import { useState } from 'react'
import { pickPstFile } from '../../api/pstApi'

interface Props {
  onAdd: (filePath: string) => void
  onClose: () => void
}

export default function PstFileAddModal({ onAdd, onClose }: Props) {
  const [filePath, setFilePath] = useState('')
  const [picking, setPicking] = useState(false)
  const [error, setError] = useState('')

  const handlePick = async () => {
    setPicking(true)
    setError('')
    try {
      const path = await pickPstFile()
      if (path) setFilePath(path)
    } catch {
      setError('파일 선택 중 오류가 발생했습니다.')
    } finally {
      setPicking(false)
    }
  }

  const handleSubmit = () => {
    const trimmed = filePath.trim()
    if (!trimmed) {
      setError('파일을 선택해주세요.')
      return
    }
    if (!trimmed.toLowerCase().endsWith('.pst')) {
      setError('.pst 파일만 등록할 수 있습니다.')
      return
    }
    setError('')
    onAdd(trimmed)
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">PST 파일 등록</h2>

        <div className="flex gap-2 mb-2">
          <input
            type="text"
            readOnly
            className="flex-1 border border-gray-300 rounded px-3 py-2 text-sm bg-gray-50 text-gray-700"
            placeholder="파일 선택 버튼을 눌러 PST 파일을 선택하세요"
            value={filePath}
          />
          <button
            onClick={handlePick}
            disabled={picking}
            className="px-4 py-2 text-sm bg-gray-100 border border-gray-300 rounded hover:bg-gray-200 disabled:opacity-50 whitespace-nowrap"
          >
            {picking ? '열기...' : '파일 선택'}
          </button>
        </div>

        {error && <p className="text-red-500 text-xs mt-1">{error}</p>}

        <div className="flex justify-end gap-2 mt-4">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800"
          >
            취소
          </button>
          <button
            onClick={handleSubmit}
            disabled={!filePath || picking}
            className="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            등록
          </button>
        </div>
      </div>
    </div>
  )
}
