import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {ShoppingList} from "../models/ShoppingList";
import {Observable} from "rxjs";

const SHOPPING_LIST_API = 'http://localhost:8080/shopping-list/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class ShoppingListService {

  constructor(private http: HttpClient) { }

  getShoppingLists(): Observable<ShoppingList[]>{
    return this.http.get<ShoppingList[]>(SHOPPING_LIST_API + 'shopping-list');
  }

  addShoppingList(shoppingList: ShoppingList){
    return this.http.post(SHOPPING_LIST_API + 'shopping-list', shoppingList);
  }

  updateShoppingList(id: number, shoppingList: ShoppingList){
    return this.http.put(SHOPPING_LIST_API + 'shopping-list/' + id, shoppingList);
  }

  deleteShoppingList(id: number){
    return this.http.delete(SHOPPING_LIST_API + 'shopping-list/' + id);
  }

}
