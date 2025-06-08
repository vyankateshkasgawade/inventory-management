import { Component, OnInit } from '@angular/core';
import { MediaFile } from '../dto/MediaFile';
import { MediaService } from '../services/media.service';


@Component({
  selector: 'app-media',
  templateUrl: './media.component.html',
  styleUrls: ['./media.component.css']
})
export class MediaComponent implements OnInit {
  mediaFiles: MediaFile[] = [];
  selectedFile: File | null = null;
  message: string = '';

  constructor(private mediaService: MediaService) { }

  ngOnInit(): void {
    this.loadMediaFiles();
  }

  loadMediaFiles(): void {
    this.mediaService.getAllMediaFiles().subscribe(
      (data: MediaFile[]) => {
        this.mediaFiles = data;
      },
      (      error: any) => {
        console.error('Error fetching media files:', error);
        this.message = 'Failed to load media files.';
      }
    );
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
  }

  uploadFile(): void {
    if (this.selectedFile) {
      this.mediaService.uploadFile(this.selectedFile).subscribe(
        (        response: any) => {
          this.message = 'File uploaded successfully!';
          this.selectedFile = null; // Clear selected file
          this.loadMediaFiles(); // Refresh the list
        },
        (        error: any) => {
          console.error('Error uploading file:', error);
          this.message = 'File upload failed!';
        }
      );
    } else {
      this.message = 'Please select a file to upload.';
    }
  }

  deleteFile(id: string): void {
    if (confirm('Are you sure you want to delete this file?')) {
      this.mediaService.deleteFile(id).subscribe(
        () => {
          this.message = 'File deleted successfully!';
          this.loadMediaFiles(); // Refresh the list
        },
        (        error: any) => {
          console.error('Error deleting file:', error);
          this.message = 'File deletion failed!';
        }
      );
    }
  }
}