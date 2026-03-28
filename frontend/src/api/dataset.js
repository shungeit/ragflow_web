import request from './request'

export function listDatasets(params) {
  return request.get('/datasets', { params })
}

export function createDataset(data) {
  return request.post('/datasets', data)
}

export function deleteDatasets(data) {
  return request.delete('/datasets', { data })
}

export function listDocuments(datasetId, params) {
  return request.get(`/datasets/${datasetId}/documents`, { params })
}

export function uploadDocuments(datasetId, formData) {
  return request.post(`/datasets/${datasetId}/documents/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteDocuments(datasetId, data) {
  return request.delete(`/datasets/${datasetId}/documents`, { data })
}

export function parseDocuments(datasetId, data) {
  return request.post(`/datasets/${datasetId}/documents/parse`, data)
}

export function stopParsingDocuments(datasetId, data) {
  return request.delete(`/datasets/${datasetId}/documents/parse`, { data })
}
