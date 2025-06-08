import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MediaFile } from '../dto/MediaFile';

@Injectable({
  providedIn: 'root'
})
export class MediaService {
  private baseUrl = 'http://localhost:8080/api/media'; // Your Spring Boot media API endpoint

  constructor(private http: HttpClient) { }

  uploadFile(file: File): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name); // 'file' must match @RequestParam name in Spring Boot

    return this.http.post(`${this.baseUrl}/upload`, formData);
  }

  getAllMediaFiles(): Observable<MediaFile[]> {
    return this.http.get<MediaFile[]>(this.baseUrl);
  }

  getMediaFileById(id: string): Observable<MediaFile> {
    return this.http.get<MediaFile>(`${this.baseUrl}/${id}`);
  }

  // Note: For 'view' endpoint, you might want to directly use the URL in the <img> or <video> tag
  // as it returns the file content directly, not JSON metadata.
  // getFileContent(id: string): Observable<Blob> {
  //   return this.http.get(`${this.baseUrl}/view/${id}`, { responseType: 'blob' });
  // }

  deleteFile(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}