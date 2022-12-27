import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShoppingListComponent } from './shopping-list.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {TranslateModule} from "@ngx-translate/core";
import {MatDialogModule} from "@angular/material/dialog";
import {TokenStorageService} from "../../services/token-storage.service";
import {of} from "rxjs";
import {ShoppingListService} from "../../services/shopping-list.service";
import {ShoppingList} from "../../models/ShoppingList";
import {EStatus} from "../../enums/EStatus";
import {ShoppingItem} from "../../models/ShoppingItem";
import {EUnit} from "../../enums/EUnit";
import {ShoppingItemService} from "../../services/shopping-item.service";

describe('ShoppingListComponent', () => {
  let component: ShoppingListComponent;
  let fixture: ComponentFixture<ShoppingListComponent>;

  let testTokenStorageService = jasmine.createSpyObj(['getToken'])
  testTokenStorageService.getToken.and.returnValue(of('sometoken'))

  let testShoppingLists: ShoppingList[] = [
    {
      "id": 1,
      "title": "Zakupy 1",
      "executionDate": new Date(),
      "status": EStatus.WAITING,
      "isEdited": true,
      "showItems": false
    }
    // {
    //   "id": 2,
    //   "title": "Zakupy 2",
    //   "executionDate": new Date(),
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "showItems": false
    // },
    // {
    //   "id": 3,
    //   "title": "Zakupy 3",
    //   "executionDate": new Date(),
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "showItems": false
    // }
  ];

  let testShoppingItems: ShoppingItem[] = [
    {
      "id": 1,
      "name": "Produkt 1",
      "quantity": 2,
      "unit": EUnit.PIECES,
      "status": EStatus.WAITING,
      "isEdited": false,
      "shoppingListId": 0,
      "hasImage": false
    }
    // {
    //   "id": 2,
    //   "name": "Produkt 2",
    //   "quantity": 150,
    //   "unit": EUnit.GRAMS,
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "shoppingListId": 0,
    //   "hasImage": false
    // },
    // {
    //   "id": 3,
    //   "name": "Produkt 3",
    //   "quantity": 1,
    //   "unit": EUnit.KILOGRAMS,
    //   "status": EStatus.WAITING,
    //   "isEdited": false,
    //   "shoppingListId": 0,
    //   "hasImage": false
    // }
  ];

  let testShoppingListService = jasmine.createSpyObj(['getShoppingLists', 'addShoppingList',
    'updateShoppingList', 'deleteShoppingList'])
  testShoppingListService.getShoppingLists.and.returnValue(of(testShoppingLists))

  let testShoppingItemService = jasmine.createSpyObj(['getShoppingItems', 'addShoppingItem',
    'updateShoppingItem', 'deleteShoppingItem'])
  testShoppingItemService.getShoppingItems.and.returnValue(of(testShoppingItems))


  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShoppingListComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        MatDialogModule
      ],
      providers: [
        { provide: TokenStorageService, useValue: testTokenStorageService },
        { provide: ShoppingListService, useValue: testShoppingListService },
        { provide: ShoppingItemService, useValue: testShoppingItemService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShoppingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getToken on tokenStorageService', () => {
    expect(testTokenStorageService.getToken).toHaveBeenCalled();
  });

  it('loggedIn should be true when token is stored', () => {
    expect(component.isLoggedIn).toBeTrue()
  });

  it('should load shoppingLists', () => {
    expect(testShoppingListService.getShoppingLists).toHaveBeenCalled()
  });

  // it('should set emptySearch true when shoppingLists return empty list', () => {
  //   testShoppingListService.getShoppingLists.and.returnValue(of([]))
  //   expect(component.emptySearchList).toBeTrue()
  // });

  it('should load shoppingItems when loadShoppingItemsListButton is called', () => {
    const btn = fixture.debugElement.nativeElement.querySelector('#loadShoppingItemsListButton')
    btn.click()
    expect(testShoppingItemService.getShoppingItems).toHaveBeenCalled()
  });

  it('should add empty list when addEmptyShoppingListButton is called', () => {
    let spy = spyOn(component, 'addEmptyShoppingList').and.callThrough()
    const btn = fixture.debugElement.nativeElement.querySelector('#addEmptyShoppingListButton')
    btn.click()
    expect(spy).toHaveBeenCalled()
  });

  // it('should call addShoppingList when updateShoppingListButton with id 0 is called', () => {
  //   let spy = spyOn(component, 'addShoppingList').withArgs(0).and.callThrough()
  //   const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingListButton')
  //   btn.click()
  //   expect(spy).toHaveBeenCalled()
  // });

  // it('should call updateShoppingList when updateShoppingListButton with id > 0 is called', () => {
  //   let spy = spyOn(component, 'addShoppingList').withArgs(1).and.callThrough()
  //   const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingListButton')
  //   btn.click()
  //   expect(spy).toHaveBeenCalled()
  // });

  it('should call editShoppingList when editShoppingListButton is called', () => {
    let spy = spyOn(component, 'editShoppingList').withArgs(1).and.callThrough()
    const btn = fixture.debugElement.nativeElement.querySelector('#editShoppingListButton')
    btn.click()
    expect(spy).toHaveBeenCalled()
  });

  it('should call deleteShoppingList when deleteShoppingListButton is called', () => {
    let spy = spyOn(component, 'deleteShoppingList').withArgs(1).and.callThrough()
    const btn = fixture.debugElement.nativeElement.querySelector('#deleteShoppingListButton')
    btn.click()
    expect(spy).toHaveBeenCalled()
  });

});
