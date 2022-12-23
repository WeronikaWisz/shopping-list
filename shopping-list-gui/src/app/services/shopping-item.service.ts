import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {ShoppingItem} from "../models/ShoppingItem";
import {ImageInfo} from "../models/ImageInfo";

const SHOPPING_ITEM_API = 'http://localhost:8080/shopping-item/';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
}

@Injectable({
  providedIn: 'root'
})
export class ShoppingItemService {

  constructor(private http: HttpClient) { }

  getShoppingItems(shoppingListId: number): Observable<ShoppingItem[]>{
    return this.http.get<ShoppingItem[]>(SHOPPING_ITEM_API + 'shopping-item/' + shoppingListId);
  }

  addShoppingItem(shoppingItem: ShoppingItem, image: any){
    const formData = new FormData();
    formData.append("image", image);
    const blobNewItem = new Blob([JSON.stringify(shoppingItem)], {
      type: 'application/json'
    })
    formData.append('info', blobNewItem);
    return this.http.post(SHOPPING_ITEM_API + 'shopping-item', formData);
  }

  updateShoppingItem(id: number, shoppingItem: ShoppingItem, image: any){
    const formData = new FormData();
    formData.append("image", image);
    const blobNewItem = new Blob([JSON.stringify(shoppingItem)], {
      type: 'application/json'
    })
    formData.append('info', blobNewItem);
    return this.http.put(SHOPPING_ITEM_API + 'shopping-item/' + id, formData);
  }

  deleteShoppingItem(id: number){
    return this.http.delete(SHOPPING_ITEM_API + 'shopping-item/' + id);
  }

  getItemImage(id: number){
    return this.http.get<ImageInfo>(SHOPPING_ITEM_API + 'shopping-item/' + id + '/image');
  }
}
