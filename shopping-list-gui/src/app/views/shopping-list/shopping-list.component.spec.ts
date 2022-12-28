import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ShoppingListComponent} from './shopping-list.component';
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
import {MatCheckboxChange, MatCheckboxModule} from "@angular/material/checkbox";
import {UsersService} from "../../services/manage-users/users.service";

describe('ShoppingListComponent initialization', () => {
  let component: ShoppingListComponent;
  let fixture: ComponentFixture<ShoppingListComponent>;

  let testTokenStorageService = jasmine.createSpyObj(['getToken'])
  testTokenStorageService.getToken.and.returnValue(of('sometoken'))

  let testShoppingListService = jasmine.createSpyObj(['getShoppingLists', 'addShoppingList',
    'updateShoppingList', 'deleteShoppingList'])
  testShoppingListService.getShoppingLists.and.returnValue(of([]))

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShoppingListComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        MatDialogModule,
        MatCheckboxModule
      ],
      providers: [
        { provide: TokenStorageService, useValue: testTokenStorageService },
        { provide: ShoppingListService, useValue: testShoppingListService }
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

  it('should add empty list when addEmptyShoppingListButton is called', () => {
    let spy = spyOn(component, 'addEmptyShoppingList').and.callThrough()
    const btn = fixture.debugElement.nativeElement.querySelector('#addEmptyShoppingListButton')
    btn.click()
    expect(spy).toHaveBeenCalled()
  });

});

describe('ShoppingListComponent manipulate shopping list', () => {
  let component: ShoppingListComponent;
  let fixture: ComponentFixture<ShoppingListComponent>;

  let testShoppingLists: ShoppingList[] = [
    {
      "id": 1,
      "title": "Zakupy 1",
      "executionDate": new Date(),
      "status": EStatus.WAITING,
      "isEdited": false,
      "showItems": false
    }
  ];

  let testShoppingListService = jasmine.createSpyObj(['getShoppingLists', 'addShoppingList',
    'updateShoppingList', 'deleteShoppingList'])
  testShoppingListService.getShoppingLists.and.returnValue(of(testShoppingLists))
  testShoppingListService.addShoppingList.and.returnValue(of("Success"))
  testShoppingListService.updateShoppingList.and.returnValue(of("Success"))
  testShoppingListService.deleteShoppingList.and.returnValue(of("Success"))

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
        { provide: ShoppingListService, useValue: testShoppingListService }
        // { provide: ShoppingItemService, useValue: testShoppingItemService }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShoppingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('changing shopping list to edit view', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });
    it('should call editShoppingList when editShoppingListButton is called', () => {
      component.shoppingLists[0].isEdited = false
      let spy = spyOn(component, 'editShoppingList').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#editShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingLists[0].isEdited).toBeTrue()
    });
  });

  describe('deleting shopping list', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });
    it('should call deleteShoppingList when deleteShoppingListButton is called', () => {
      component.shoppingLists[0].isEdited = false
      let spy = spyOn(component, 'deleteShoppingList').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#deleteShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingLists).toHaveSize(0)
    });
  });

  describe('cancel adding new list', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 0,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": true,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });
    it('should delete shopping list when cancelUpdateShoppingListButton with id = 0 is called', () => {
      let spy = spyOn(component, 'cancelUpdateShoppingList').withArgs(0).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#cancelUpdateShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingLists).toHaveSize(0)
    });
  });

  describe('adding new shopping list', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 0,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": true,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });
    it('should add shopping list when updateShoppingListButton with id = 0 is called', () => {
      let spy = spyOn(component, 'addShoppingList').withArgs(0).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(testShoppingListService.addShoppingList).toHaveBeenCalled()
      expect(component.shoppingLists[0].isEdited).toBeFalse()
    });
  });

  describe('canceling update shopping list', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited":  true,
          "showItems": false
        }
      ];
      component.editedExecutionDate = new Date()
      fixture.detectChanges()
    });

    it('should cancel update shopping list when cancelUpdateShoppingListButton with id > 0 is called', () => {
      let spy = spyOn(component, 'cancelUpdateShoppingList').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#cancelUpdateShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
    });

  });

  describe('updating shopping list set accomplished', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.ACCOMPLISHED,
          "isEdited": true,
          "showItems": false
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });

    it('should update shopping list when updateShoppingListButton with id > 0 is called', () => {
      let spy = spyOn(component, 'updateShoppingList').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(testShoppingListService.updateShoppingList).toHaveBeenCalled()
      expect(component.shoppingLists[0].isEdited).toBeFalse()
      expect(component.shoppingItems[0].status).toEqual(EStatus.ACCOMPLISHED)
    });

  });

  describe('updating shopping list set waiting', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": true,
          "showItems": false
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.ACCOMPLISHED,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });

    it('should update shopping list when updateShoppingListButton with id > 0 is called', () => {
      let spy = spyOn(component, 'updateShoppingList').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(testShoppingListService.updateShoppingList).toHaveBeenCalled()
      expect(component.shoppingLists[0].isEdited).toBeFalse()
      expect(component.shoppingItems[0].status).toEqual(EStatus.WAITING)
    });

  });

  describe('checking shopping list', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });

    it('should check shopping list when status is accomplished', () => {
      component.shoppingLists[0].status = EStatus.ACCOMPLISHED
      let event = new MatCheckboxChange()
      event.checked = false
      component.listCheckboxChange(event, 1)
      fixture.detectChanges()
      expect(component.shoppingLists[0].status).toEqual(0)
    });

    it('should not check shopping list when status is waiting', () => {
      component.shoppingLists[0].status = EStatus.WAITING
      let event = new MatCheckboxChange()
      event.checked = true
      component.listCheckboxChange(event, 1)
      fixture.detectChanges()
      expect(component.shoppingLists[0].status).toEqual(2)
    });

  });

});

describe('ShoppingListComponent manipulate shopping item', () => {
  let component: ShoppingListComponent;
  let fixture: ComponentFixture<ShoppingListComponent>;

  let testShoppingLists: ShoppingList[] = [
    {
      "id": 1,
      "title": "Zakupy 1",
      "executionDate": new Date(),
      "status": EStatus.WAITING,
      "isEdited": false,
      "showItems": false
    }
  ];

  let testShoppingItems: ShoppingItem[] = [
    {
      "id": 1,
      "name": "Produkt 1",
      "quantity": 2,
      "unit": EUnit.PIECES,
      "status": EStatus.WAITING,
      "isEdited": false,
      "shoppingListId": 1,
      "hasImage": false
    }
  ];

  let testShoppingListService = jasmine.createSpyObj(['getShoppingLists', 'addShoppingList',
    'updateShoppingList', 'deleteShoppingList'])
  testShoppingListService.getShoppingLists.and.returnValue(of(testShoppingLists))
  testShoppingListService.updateShoppingList.and.returnValue(of("Success"))

  let testShoppingItemService = jasmine.createSpyObj(['getShoppingItems', 'addShoppingItem',
    'updateShoppingItem', 'deleteShoppingItem', 'getItemImage'])
  testShoppingItemService.getShoppingItems.and.returnValue(of(testShoppingItems))
  testShoppingItemService.addShoppingItem.and.returnValue(of("Success"))
  testShoppingItemService.updateShoppingItem.and.returnValue(of("Success"))
  testShoppingItemService.deleteShoppingItem.and.returnValue(of("Success"))
  testShoppingItemService.getItemImage.and.returnValue(of(1))

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

  describe('loading shopping items', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": false
        },
        {
          "id": 2,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });

    it('should load shoppingItems when loadShoppingItemsListButton is called', () => {
      const btn = fixture.debugElement.nativeElement.querySelector('#loadShoppingItemsListButton')
      btn.click()
      fixture.detectChanges()
      expect(component.shoppingItems).toHaveSize(1)
      expect(component.shoppingLists[0].showItems).toBeTrue()
    });

  });

  describe('changing shopping item to edit view', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.KILOGRAMS,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });
    it('should call editShoppingItem when editShoppingItemButton is called', () => {
      component.shoppingItems[0].isEdited = false
      let spy = spyOn(component, 'editShoppingItem').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#editShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingItems[0].isEdited).toBeTrue()
    });
  });

  describe('deleting shopping item', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });
    it('should call deleteShoppingItem when deleteShoppingItemButton is called', () => {
      let spy = spyOn(component, 'deleteShoppingItem').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#deleteShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingItems).toHaveSize(0)
    });
  });

  describe('cancel adding new item', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 0,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 0,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": true,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });
    it('should delete shopping item when cancelUpdateShoppingItemButton with id = 0 is called', () => {
      let spy = spyOn(component, 'cancelUpdateShoppingItem').withArgs(0).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#cancelUpdateShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingItems).toHaveSize(0)
    });
  });

  describe('adding empty shopping item', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      fixture.detectChanges()
    });
    it('should add empty item when addEmptyShoppingItemButton is called', () => {
      let spy = spyOn(component, 'addEmptyItemToShoppingList').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#addEmptyShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingItems).toHaveSize(1)
    });
  });

  describe('adding new shopping item', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 0,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 0,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": true,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });
    it('should add shopping item when updateShoppingItemButton with id = 0 is called', () => {
      let spy = spyOn(component, 'addItemToShoppingList').withArgs(0).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingItems[0].isEdited).toBeFalse()
    });
  });

  describe('canceling update shopping list', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited":  false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": true,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      component.editedUnit = EUnit.PIECES
      fixture.detectChanges()
    });

    it('should cancel update shopping item when cancelUpdateShoppingItemButton with id > 0 is called', () => {
      let spy = spyOn(component, 'cancelUpdateShoppingItem').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#cancelUpdateShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
    });

  });

  describe('updating shopping item set accomplished', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.GRAMS,
          "status": EStatus.ACCOMPLISHED,
          "isEdited": true,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });

    it('should update shopping item when updateShoppingItemButton with id > 0 is called', () => {
      let spy = spyOn(component, 'updateShoppingItem').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingItems[0].isEdited).toBeFalse()
      expect(component.shoppingLists[0].status).toEqual(EStatus.ACCOMPLISHED)
    });

  });

  describe('updating shopping item set waiting', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.ACCOMPLISHED,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": true,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });

    it('should update shopping item when updateShoppingItemButton with id > 0 is called', () => {
      let spy = spyOn(component, 'updateShoppingItem').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
      expect(component.shoppingItems[0].isEdited).toBeFalse()
      expect(component.shoppingLists[0].status).toEqual(EStatus.WAITING)
    });

  });

  describe('checking shopping item', () => {

    beforeEach(function () {
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });

    it('should check shopping item when status is accomplished', () => {
      component.shoppingItems[0].status = EStatus.ACCOMPLISHED
      let event = new MatCheckboxChange()
      event.checked = false
      component.itemCheckboxChange(event, 1)
      fixture.detectChanges()
      expect(component.shoppingItems[0].status).toEqual(0)
    });

    it('should not check shopping item when status is waiting', () => {
      component.shoppingItems[0].status = EStatus.WAITING
      let event = new MatCheckboxChange()
      event.checked = true
      component.itemCheckboxChange(event, 1)
      fixture.detectChanges()
      expect(component.shoppingItems[0].status).toEqual(2)
    });
  });

  describe('showing item image', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.ACCOMPLISHED,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.LITERS,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": true
        }
      ]
      fixture.detectChanges()
    });

    it('should show item image when showItemImageButton click', () => {
      let spy = spyOn(component, 'showItemImage').and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#showItemImageButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
    });
  });

  // describe('file manipulation', () => {
  //   it('should set file when on file selected', () => {
  //     let event = {target:{files:[1]}}
  //     component.onFileSelected(event)
  //     fixture.detectChanges()
  //     expect(testShoppingItemService.getItemImage).toHaveBeenCalled()
  //   });
  // });

});

describe('ShoppingListComponent integration test with ShoppingItemService', () => {
  let component: ShoppingListComponent;
  let fixture: ComponentFixture<ShoppingListComponent>;
  let service: ShoppingItemService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShoppingListComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        MatDialogModule
      ],
    })
      .compileComponents();
    service = TestBed.inject(ShoppingItemService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShoppingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('load shopping items', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": false
        }
      ]
      fixture.detectChanges()
    });

    it('loadShoppingItemsListButton should call service getShoppingItems', () => {
      let spy = spyOn(service, 'getShoppingItems').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#loadShoppingItemsListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(1)
    });

  });

  describe('delete shopping item', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.ACCOMPLISHED,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });
    it('deleteShoppingItemButton should call service deleteShoppingItem', () => {
      let spy = spyOn(service, 'deleteShoppingItem').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#deleteShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(1)
    });
  });

  describe('update shopping item', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.ACCOMPLISHED,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.PIECES,
          "status": EStatus.WAITING,
          "isEdited": true,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });
    it('updateShoppingItemButton should call service updateShoppingItem if id > 0', () => {
      let spy = spyOn(service, 'updateShoppingItem').withArgs(1, component.shoppingItems[0], component.file).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(1, component.shoppingItems[0], component.file)
    });
  });

  describe('add shopping item', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 0,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.GRAMS,
          "status": EStatus.ACCOMPLISHED,
          "isEdited": true,
          "shoppingListId": 1,
          "hasImage": false
        }
      ]
      fixture.detectChanges()
    });
    it('updateShoppingItemButton should call service addShoppingItem if id = 0', () => {
      let spy = spyOn(service, 'addShoppingItem').withArgs(component.shoppingItems[0], component.file).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingItemButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(component.shoppingItems[0], component.file)
    });
  });

  describe('get item image', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.ACCOMPLISHED,
          "isEdited": false,
          "showItems": true
        }
      ];
      component.shoppingItems = [
        {
          "id": 1,
          "name": "Produkt 1",
          "quantity": 2,
          "unit": EUnit.LITERS,
          "status": EStatus.WAITING,
          "isEdited": false,
          "shoppingListId": 1,
          "hasImage": true
        }
      ]
      fixture.detectChanges()
    });

    it('showItemImageButton should call service getItemImage', () => {
      let spy = spyOn(service, 'getItemImage').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#showItemImageButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(1)
    });
  });

});

describe('ShoppingListComponent integration test with ShoppingListService', () => {
  let component: ShoppingListComponent;
  let fixture: ComponentFixture<ShoppingListComponent>;
  let service: ShoppingListService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShoppingListComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        MatDialogModule
      ],
    })
      .compileComponents();
    service = TestBed.inject(ShoppingListService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShoppingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('load shopping lists', () => {

    it('should call service getShoppingLists', () => {
      let spy = spyOn(service, 'getShoppingLists').and.callThrough()
      component.loadShoppingLists()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalled()
    });

  });

  describe('delete shopping list', () => {
    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": false,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });
    it('deleteShoppingListButton should call service deleteShoppingList', () => {
      let spy = spyOn(service, 'deleteShoppingList').withArgs(1).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#deleteShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(1)
    });
  });

  describe('update shopping list', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 1,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.ACCOMPLISHED,
          "isEdited": true,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });
    it('updateShoppingListmButton should call service updateShoppingList if id > 0', () => {
      let spy = spyOn(service, 'updateShoppingList').withArgs(1, component.shoppingLists[0]).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(1, component.shoppingLists[0])
    });
  });

  describe('add shopping list', () => {

    beforeEach(function () {
      component.shoppingLists = [
        {
          "id": 0,
          "title": "Zakupy 1",
          "executionDate": new Date(),
          "status": EStatus.WAITING,
          "isEdited": true,
          "showItems": false
        }
      ];
      fixture.detectChanges()
    });
    it('updateShoppingListButton should call service addShoppingList if id = 0', () => {
      let spy = spyOn(service, 'addShoppingList').withArgs(component.shoppingLists[0]).and.callThrough()
      const btn = fixture.debugElement.nativeElement.querySelector('#updateShoppingListButton')
      btn.click()
      fixture.detectChanges()
      expect(spy).toHaveBeenCalledWith(component.shoppingLists[0])
    });
  });

});

