import axios from 'axios'
import type { MailDetail, SearchPage, SearchRequest } from '../types'

const BASE = '/api/search'

export const searchMails = (req: SearchRequest): Promise<SearchPage> => {
  const params: Record<string, string | number> = {}
  if (req.subject)        params.subject        = req.subject
  if (req.body)           params.body           = req.body
  if (req.senderEmail)    params.senderEmail    = req.senderEmail
  if (req.senderName)     params.senderName     = req.senderName
  if (req.recipientEmail) params.recipientEmail = req.recipientEmail
  if (req.recipientName)  params.recipientName  = req.recipientName
  if (req.dateFrom)       params.dateFrom       = req.dateFrom
  if (req.dateTo)         params.dateTo         = req.dateTo
  if (req.attachment)     params.attachment     = req.attachment
  params.page = req.page ?? 0
  params.size = req.size ?? 50

  return axios.get<SearchPage>(BASE, { params }).then((r) => r.data)
}

export const getMailDetail = (mailId: number): Promise<MailDetail> =>
  axios.get<MailDetail>(`${BASE}/${mailId}`).then((r) => r.data)
