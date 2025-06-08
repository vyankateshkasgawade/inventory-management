import { Component, OnInit } from '@angular/core';
import { ProductCategoryService, ProductCategory } from '../services/product-category.service';
//import { FormsModule } from '@angular/forms';
//import { FormsModule, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // FormsModule
import { CommonModule } from '@angular/common'; // CommonModule
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
   standalone: true, 
  selector: 'app-product-category',
  templateUrl: './product-category.component.html',
  styleUrls: ['./product-category.component.css'],
   imports: [
    FormsModule,
    CommonModule,
     CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTooltipModule,
    MatDividerModule
  ], 
})
export class ProductCategoryComponent implements OnInit {
  categories: ProductCategory[] = [];
  newCategory: ProductCategory = { productCategoryName: '' };
  editingCategoryId: number | null = null;
  updatedCategoryName: string = '';

  constructor(private categoryService: ProductCategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(data => {
      this.categories = data;
    });
  }

  createCategory(): void {
    if (this.newCategory.productCategoryName.trim()) {
      this.categoryService.createCategory(this.newCategory).subscribe(() => {
        this.loadCategories();
        this.newCategory.productCategoryName = '';
      });
    }
  }

  startEdit(category: ProductCategory): void {
    this.editingCategoryId = category.productCategoryId!;
    this.updatedCategoryName = category.productCategoryName;
  }

  updateCategory(): void {
    if (this.editingCategoryId && this.updatedCategoryName.trim()) {
      const updated = { productCategoryName: this.updatedCategoryName };
      this.categoryService.updateCategory(this.editingCategoryId, updated).subscribe(() => {
        this.editingCategoryId = null;
        this.loadCategories();
      });
    }
  }

  cancelEdit(): void {
    this.editingCategoryId = null;
  }

  deleteCategory(id: number): void {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe(() => {
        this.loadCategories();
      });
    }
  }
}
