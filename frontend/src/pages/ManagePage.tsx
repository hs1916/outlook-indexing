import { useEffect, useState } from 'react'
import { addPstFile, deletePstFile, getPstFiles } from '../api/pstApi'
import PstFileAddModal from '../components/manage/PstFileAddModal'
import PstFileTable from '../components/manage/PstFileTable'
import type { PstFile } from '../types'

export default function ManagePage() {
  const [files, setFiles] = useState<PstFile[]>([])
  const [showModal, setShowModal] = useState(false)
  const [error, setError] = useState('')

  const fetchFiles = () => {
    getPstFiles().then(setFiles).catch(() => setError('파일 목록을 불러오지 못했습니다.'))
  }

  useEffect(() => {
    fetchFiles()
  }, [])

  const handleAdd = async (filePath: string) => {
    try {
      await addPstFile(filePath)
      setShowModal(false)
      fetchFiles()
    } catch (e: unknown) {
      const msg =
        (e as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        '파일 등록에 실패했습니다.'
      setError(msg)
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await deletePstFile(id)
      fetchFiles()
    } catch {
      setError('삭제에 실패했습니다.')
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-xl font-bold text-gray-800">PST 파일 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            PST 파일을 등록하고 인덱싱을 실행하세요.
          </p>
        </div>
        <button
          onClick={() => {
            setError('')
            setShowModal(true)
          }}
          className="px-4 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700"
        >
          + 파일 추가
        </button>
      </div>

      {error && (
        <div className="mb-3 p-3 bg-red-50 border border-red-200 text-red-600 text-sm rounded">
          {error}
          <button onClick={() => setError('')} className="ml-2 text-red-400 hover:text-red-600">
            ×
          </button>
        </div>
      )}

      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
        <PstFileTable files={files} onDelete={handleDelete} onRefresh={fetchFiles} />
      </div>

      {showModal && (
        <PstFileAddModal onAdd={handleAdd} onClose={() => setShowModal(false)} />
      )}
    </div>
  )
}
