export interface MediaFile {
  id: string;
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  filePath: string;
  fileUrl: string;
  uploadDate: string; // Use string for date as it comes as ISO string from backend
}