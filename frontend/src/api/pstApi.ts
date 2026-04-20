import axios from 'axios'
import type { PstFile, IndexProgress } from '../types'

const BASE = '/api/pst-files'

export const pickPstFile = (): Promise<string | null> =>
  axios.get<{ filePath: string }>('/api/file-picker')
    .then((r) => r.data.filePath)
    .catch((e) => {
      if (e.response?.status === 204) return null
      throw e
    })

export const getPstFiles = (): Promise<PstFile[]> =>
  axios.get<PstFile[]>(BASE).then((r) => r.data)

export const addPstFile = (filePath: string): Promise<PstFile> =>
  axios.post<PstFile>(BASE, { filePath }).then((r) => r.data)

export const deletePstFile = (id: number): Promise<void> =>
  axios.delete(`${BASE}/${id}`).then(() => undefined)

export const startIndexing = (id: number): Promise<void> =>
  axios.post(`${BASE}/${id}/index`).then(() => undefined)

export const subscribeProgress = (
  id: number,
  onProgress: (p: IndexProgress) => void,
  onDone?: () => void,
  onError?: () => void,
): EventSource => {
  const es = new EventSource(`${BASE}/${id}/index/progress`)

  es.onmessage = (e) => {
    const data: IndexProgress = JSON.parse(e.data)
    onProgress(data)
    if (data.status === 'DONE' || data.status === 'ERROR') {
      es.close()
      if (data.status === 'DONE') onDone?.()
      else onError?.()
    }
  }

  es.onerror = () => {
    es.close()
    onError?.()
  }

  return es
}
