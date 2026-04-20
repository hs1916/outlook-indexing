export type IndexStatus = 'PENDING' | 'INDEXING' | 'DONE' | 'ERROR'

export interface PstFile {
  id: number
  filePath: string
  fileName: string
  fileSizeBytes: number
  status: IndexStatus
  totalMailCount: number
  indexedMailCount: number
  errorCount: number
  elapsedSeconds: number
  indexedAt: string | null
  createdAt: string
}

export interface IndexProgress {
  indexedCount: number
  totalCount: number
  percent: number
  status: IndexStatus
  errorCount?: number
  elapsedSeconds?: number
}

export interface SearchRequest {
  subject?: string
  body?: string
  senderEmail?: string
  senderName?: string
  recipientEmail?: string
  recipientName?: string
  dateFrom?: string
  dateTo?: string
  attachment?: string
  page?: number
  size?: number
}

export interface SearchResult {
  id: number
  subject: string
  senderEmail: string | null
  senderName: string | null
  sentDate: string | null
  hasAttachment: boolean
  pstFileName: string
}

export interface MailDetail {
  id: number
  subject: string
  senderEmail: string | null
  senderName: string | null
  recipientEmails: string | null
  recipientNames: string | null
  sentDate: string | null
  body: string
  attachmentNames: string[]
  pstFileName: string
}

export interface SearchPage {
  totalElements: number
  totalPages: number
  number: number
  size: number
  content: SearchResult[]
}
