import {Component, OnInit} from '@angular/core';
import {TokenStorageService} from "../../services/token-storage.service";
import {Router} from "@angular/router";
import {ShoppingList} from "../../models/ShoppingList";
import {EStatus} from "../../enums/EStatus";
import {MatCheckboxChange} from "@angular/material/checkbox";
import {ShoppingListService} from "../../services/shopping-list.service";
import Swal from "sweetalert2";
import {TranslateService} from "@ngx-translate/core";
import {ShoppingItem} from "../../models/ShoppingItem";
import {EUnit} from "../../enums/EUnit";
import {ShoppingItemService} from "../../services/shopping-item.service";
import {Unit} from "../../models/Unit";
import {formatDate} from "@angular/common";
import {MatDialog} from "@angular/material/dialog";
import {ImageDialogComponent} from "../image-dialog/image-dialog.component";

@Component({
  selector: 'app-shopping-list',
  templateUrl: './shopping-list.component.html',
  styleUrls: ['./shopping-list.component.sass']
})
export class ShoppingListComponent implements OnInit {

  isLoggedIn = false;
  shoppingLists: ShoppingList[] = [
    // {
    //   "id": 1,
    //   "title": "Zakupy 1",
    //   "executionDate": "2022-09-18",
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "showItems": false
    // },
    // {
    //   "id": 2,
    //   "title": "Zakupy 2",
    //   "executionDate": "2022-09-18",
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "showItems": false
    // },
    // {
    //   "id": 3,
    //   "title": "Zakupy 3",
    //   "executionDate": "2022-09-18",
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "showItems": false
    // }
  ];

  shoppingItems: ShoppingItem[] = [
    // {
    //   "id": 1,
    //   "name": "Produkt 1",
    //   "quantity": 2,
    //   "unit": EUnit.PIECES,
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "shoppingListId": 0
    // },
    // {
    //   "id": 2,
    //   "name": "Produkt 2",
    //   "quantity": 150,
    //   "unit": EUnit.GRAMS,
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "shoppingListId": 0
    // },
    // {
    //   "id": 3,
    //   "name": "Produkt 3",
    //   "quantity": 1,
    //   "unit": EUnit.KILOGRAMS,
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "shoppingListId": 0
    // }
  ];

  emptySearchList = false;

  emptyItemsList = false;

  editedTitle: string = "";
  editedExecutionDate?: Date;

  editedName: string = "";
  editedQuantity: number = 0;
  editedUnit?: EUnit;

  units: Unit[] = [];

  file: File | null = null;
  fileName: string = '';

  image: any;

  format = 'dd/MM/yyyy';
  locale = 'en-US';

  constructor(private tokenStorage: TokenStorageService, private router: Router, private translate: TranslateService,
              private shoppingListService: ShoppingListService, private shoppingItemService: ShoppingItemService,
              public dialog: MatDialog) {
  }

  ngOnInit(): void {
    if (this.tokenStorage.getToken()) {
      this.isLoggedIn = true;
    } else {
      this.router.navigate(['/login']).then(() => this.reloadPage());
    }
    this.loadShoppingLists()
  }


  reloadPage(): void {
    window.location.reload();
  }

  loadUnits(){
    this.units = [
      {
        unit: EUnit.PIECES,
        name: this.getTranslateMessage("shopping-item.unit-pieces")
      },
      {
        unit: EUnit.LITERS,
        name: this.getTranslateMessage("shopping-item.unit-l")
      },
      {
        unit: EUnit.KILOGRAMS,
        name: this.getTranslateMessage("shopping-item.unit-kg")
      },
      {
        unit: EUnit.GRAMS,
        name: this.getTranslateMessage("shopping-item.unit-g")
      }];
  }

  loadShoppingLists(){
    this.emptySearchList = false;
    this.shoppingListService.getShoppingLists().subscribe(
        data => {
          console.log(data);
          data.forEach(shoppingList => shoppingList.isEdited = false)
          data.forEach(shoppingItem => shoppingItem.showItems = false)
          this.shoppingLists = data;
          if(this.shoppingLists.length == 0){
            this.emptySearchList = true;
          }
          this.loadUnits()
        },
        err => {
          console.log(err)
        }
      )
  }

  public loadShoppingItemsList(id: number, isAddNewItem: boolean){
    this.emptyItemsList = false;
    let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
    if(!isAddNewItem) {
      this.closeOtherShoppingItems(index)
    }
    if(!isAddNewItem && this.shoppingLists[index].showItems){
      this.shoppingLists[index].showItems = false;
    } else {
      this.shoppingItemService.getShoppingItems(id).
        subscribe(
        data => {
          console.log(data);
          data.forEach(shoppingItem => shoppingItem.isEdited = false)
          data.forEach(shoppingItem => shoppingItem.unit = this.getUnitEnum(shoppingItem.unit))
          this.shoppingItems = data;
          console.log(this.shoppingItems)
          if(this.shoppingItems.length == 0){
            this.emptyItemsList = true;
          }
          this.shoppingLists[index].showItems = true;
        },
        err => {
          console.log(err)
        }
      );
    }
  }

  addEmptyShoppingList(){
    let shoppingList: ShoppingList = {
      "id": 0,
      "title": "",
      "executionDate": new Date(),
      "status": EStatus.WAITING,
      "isEdited": true,
      "showItems": false
    }
    this.shoppingLists.unshift(shoppingList);
    this.emptySearchList = false;
    this.closeOtherShoppingItems(0);
  }

  addShoppingList(id: number){
    let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
    this.shoppingListService.addShoppingList(this.shoppingLists[index])
      .subscribe(
        data => {
          console.log(data);
          this.shoppingLists[index].isEdited = false;
          this.loadShoppingLists();
        },
        err => {
          console.log(err)
        }
      )
  }

  editShoppingList(id: number){
    let editedListId = this.shoppingLists.findIndex(shoppingList => shoppingList.isEdited)
    if(editedListId >= 0){
      this.cancelUpdateShoppingList(this.shoppingLists[editedListId].id);
    }
    let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
    this.editedTitle = this.shoppingLists[index].title;
    this.editedExecutionDate = this.shoppingLists[index].executionDate;
    this.shoppingLists[index].isEdited = true;
  }

  cancelUpdateShoppingList(id: number){
    let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
    if(id == 0){
      this.shoppingLists.splice(index,1);
    } else {
      this.shoppingLists[index].title = this.editedTitle;
      this.shoppingLists[index].executionDate = this.editedExecutionDate!;
      this.shoppingLists[index].isEdited = false;
      this.editedTitle = "";
      this.editedExecutionDate = undefined;
    }
  }

  updateShoppingList(id: number){
    let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
    if(id == 0){
      this.addShoppingList(id);
    } else {
      this.shoppingListService.updateShoppingList(id, this.shoppingLists[index])
        .subscribe(
          data => {
            console.log(data);
            this.shoppingLists[index].isEdited = false;
            if(!this.isStatusWaiting(this.shoppingLists[index].status)){
              this.shoppingItems.forEach(shoppingItem => {
                if(shoppingItem.shoppingListId == id) {
                  shoppingItem.status = EStatus.ACCOMPLISHED }
              }  )
            } else if(this.isStatusWaiting(this.shoppingLists[index].status)){
              this.shoppingItems.forEach(shoppingItem => {
                if(shoppingItem.shoppingListId == id) {
                  shoppingItem.status = EStatus.WAITING }
              }  )
            }
          },
          err => {
            console.log(err)
          }
        )
    }
  }

  deleteShoppingList(id: number){
    this.shoppingListService.deleteShoppingList(id).subscribe(
      data => {
        console.log(data);
        let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
        this.shoppingLists.splice(index,1);
      },
      err => {
        console.log(err)
      }
    )
  }

  isStatusWaiting(status: EStatus): boolean{
    let statusS = status.valueOf() as unknown as string;
    return (status == EStatus.WAITING || statusS === EStatus[EStatus.WAITING])
  }

  isShoppingListEdited(): boolean{
    return this.shoppingLists.some(shoppingList => shoppingList.isEdited)
  }

  isShoppingListValid(id: number): boolean{
    let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
    return (this.shoppingLists[index].title != "" && this.shoppingLists[index].executionDate != null)
  }

  listCheckboxChange(event: MatCheckboxChange, id: number){
    let index = this.shoppingLists.findIndex(shoppingList => shoppingList.id === id);
    if(event.checked){
      this.shoppingLists[index].status = EStatus.ACCOMPLISHED;
    } else {
      this.shoppingLists[index].status = EStatus.WAITING;
    }
    this.updateShoppingList(id);
  }


  isShoppingItemEdited(): boolean{
    return this.shoppingItems.some(shoppingItem => shoppingItem.isEdited)
  }

  isShoppingItemValid(id: number): boolean{
    let index = this.shoppingItems.findIndex(shoppingItem => shoppingItem.id === id);
    return this.shoppingItems[index].name != ""
  }

  itemCheckboxChange(event: MatCheckboxChange, id: number){
    let index = this.shoppingItems.findIndex(shoppingItem => shoppingItem.id === id);
    if(event.checked){
      this.shoppingItems[index].status = EStatus.ACCOMPLISHED;
    } else {
      this.shoppingItems[index].status = EStatus.WAITING;
    }
    this.updateShoppingItem(id);
  }

  editShoppingItem(id: number){
    let editedItemId = this.shoppingItems.findIndex(shoppingItem => shoppingItem.isEdited)
    if(editedItemId >= 0){
      this.cancelUpdateShoppingItem(this.shoppingItems[editedItemId].id)
    }
    let index = this.shoppingItems.findIndex(shoppingItem => shoppingItem.id === id);
    this.editedName = this.shoppingItems[index].name;
    this.editedQuantity = this.shoppingItems[index].quantity;
    this.editedUnit = this.shoppingItems[index].unit;
    this.shoppingItems[index].isEdited = true;
  }

  cancelUpdateShoppingItem(id: number){
    let index = this.shoppingItems.findIndex(shoppingItem => shoppingItem.id === id);
    if(id == 0){
      this.shoppingItems.splice(index,1);
    } else {
      this.shoppingItems[index].name = this.editedName;
      this.shoppingItems[index].quantity = this.editedQuantity;
      this.shoppingItems[index].unit = this.editedUnit!
      this.shoppingItems[index].isEdited = false;
      this.editedName = "";
      this.editedQuantity = 0;
      this.editedUnit = undefined;
    }
    this.file = null
    this.image = null
    this.fileName = '';
  }

  updateShoppingItem(id: number){
    let index = this.shoppingItems.findIndex(shoppingItem => shoppingItem.id === id);
    if(id == 0){
      this.addItemToShoppingList(id)
    } else {
      this.shoppingItemService.updateShoppingItem(id, this.shoppingItems[index], this.file)
        .subscribe(
          data => {
            console.log(data);
            this.shoppingItems[index].isEdited = false;
            if(this.file != null){
              this.shoppingItems[index].hasImage = true;
            }
            this.file = null
            this.image = null
            this.fileName = '';
            let listIndex = this.shoppingLists.findIndex(
              shoppingList => shoppingList.id == this.shoppingItems[index].shoppingListId);
            if(this.shoppingItems.every(shoppingItem => !this.isStatusWaiting(shoppingItem.status))){
              this.shoppingLists[listIndex].status = EStatus.ACCOMPLISHED;
            } else if(this.shoppingItems.some(shoppingItem => this.isStatusWaiting(shoppingItem.status))) {
              this.shoppingLists[listIndex].status = EStatus.WAITING;
            }
          },
          err => {
            console.log(err)
          }
        )
    }
  }

  addEmptyItemToShoppingList(shoppingListId: number){
    let shoppingItem: ShoppingItem = {
      "id": 0,
      "name": "",
      "quantity": 1,
      "unit": EUnit.PIECES,
      "status": EStatus.WAITING,
      "isEdited": true,
      "shoppingListId": shoppingListId,
      "hasImage": false
    }
    this.shoppingItems.push(shoppingItem);
    this.emptyItemsList = false;
  }

  addItemToShoppingList(id: number){
    let index = this.shoppingItems.findIndex(shoppingItem => shoppingItem.id === id);
    this.shoppingItemService.addShoppingItem(this.shoppingItems[index], this.file)
      .subscribe(
        data => {
          console.log(data);
          this.shoppingItems[index].isEdited = false;
          this.file = null
          this.image = null
          this.fileName = '';
          this.loadShoppingItemsList(this.shoppingItems[index].shoppingListId, true)
        },
        err => {
          console.log(err)
        }
      )
  }

  showItemImage(id: number){
    this.shoppingItemService.getItemImage(id)
      .subscribe(
        data => {
          console.log(data);
          const dialogRef = this.dialog.open(ImageDialogComponent, {
            data: {
              name: data.name,
              image: data.image
            }
          });
          dialogRef.afterClosed().subscribe(result => {
            console.log(result);
          });
        },
        err => {
          console.log(err)
        }
      )
  }

  deleteShoppingItem(id: number){
    this.shoppingItemService.deleteShoppingItem(id).subscribe(
      data => {
        console.log(data);
        let index = this.shoppingItems.findIndex(shoppingItem => shoppingItem.id === id);
        this.shoppingItems.splice(index,1);
      },
      err => {
        console.log(err)
      }
    )
  }

  closeOtherShoppingItems(index: number){
    let listIndex = this.shoppingLists.findIndex(shoppingList => shoppingList.showItems);
    if(listIndex >= 0 && index != listIndex) {
        let itemIndex = this.shoppingItems.findIndex(shoppingItem => shoppingItem.isEdited);
        if (itemIndex >= 0) {
          this.cancelUpdateShoppingItem(itemIndex);
        }
        this.shoppingLists[listIndex].showItems = false;
    }
  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];
    if (this.file) {
      this.fileName = this.file.name;
    } else {
      this.fileName = '';
    }
  }

  getUnit(unit: EUnit){
    let unitS = unit.valueOf() as unknown as string;
    if(unitS == EUnit[EUnit.LITERS] || unit == EUnit.LITERS){
      return this.getTranslateMessage("shopping-item.unit-l")
    } else if(unitS == EUnit[EUnit.PIECES] || unit == EUnit.PIECES){
      return this.getTranslateMessage("shopping-item.unit-pieces")
    } else if(unitS == EUnit[EUnit.GRAMS] || unit == EUnit.GRAMS){
      return this.getTranslateMessage("shopping-item.unit-g")
    } else if(unitS == EUnit[EUnit.KILOGRAMS] || unit == EUnit.KILOGRAMS){
      return this.getTranslateMessage("shopping-item.unit-kg")
    } else {
      return ''
    }
  }

  getUnitEnum(unit: EUnit){
    let unitS = unit.valueOf() as unknown as string;
    if(unitS == EUnit[EUnit.LITERS]){
      return EUnit.LITERS
    } else if(unitS == EUnit[EUnit.GRAMS]){
      return EUnit.GRAMS
    } else if(unitS == EUnit[EUnit.KILOGRAMS]){
      return EUnit.KILOGRAMS
    } else {
      return EUnit.PIECES
    }
  }

  formatExecutionDate(date: Date): string{
    return formatDate(date, this.format, this.locale);
  }

  getTranslateMessage(key: string): string{
    let message = "";
    this.translate.get(key).subscribe(data =>
      message = data
    );
    return message;
  }

}
