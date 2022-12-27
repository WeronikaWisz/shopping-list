import {TestBed} from '@angular/core/testing';

import {ShoppingItemService} from './shopping-item.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {ShoppingItem} from "../models/ShoppingItem";
import {EStatus} from "../enums/EStatus";
import {EUnit} from "../enums/EUnit";

describe('ShoppingItemService', () => {
  let service: ShoppingItemService;
  let httpMock: HttpTestingController;

  let testShoppingItem : ShoppingItem = {
    hasImage: false,
    id: 0,
    isEdited: false,
    name: "",
    quantity: 0,
    shoppingListId: 0,
    status: EStatus.WAITING,
    unit: EUnit.PIECES
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    service = TestBed.inject(ShoppingItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getShoppingLists should GET on shopping-item/:shoppingItemId', (done) => {
    service.getShoppingItems(1).subscribe(res => done());
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-item/shopping-item/1');
    expect(successRequest.request.method).toEqual('GET');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('addShoppingItem should POST on shopping-item', (done) => {
    service.addShoppingItem(testShoppingItem, 1).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-item/shopping-item');
    expect(successRequest.request.method).toEqual('POST');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('updateShoppingItem should PUT on shopping-item/id', (done) => {
    service.updateShoppingItem(1, testShoppingItem, 1).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-item/shopping-item/1');
    expect(successRequest.request.method).toEqual('PUT');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('deleteShoppingItem should DELETE on shopping-item/id', (done) => {
    service.deleteShoppingItem(1).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-item/shopping-item/1');
    expect(successRequest.request.method).toEqual('DELETE');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('getItemImage should GET on shopping-item/:id/image', (done) => {
    service.getItemImage(1).subscribe(res => done());
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-item/shopping-item/1/image');
    expect(successRequest.request.method).toEqual('GET');
    successRequest.flush(null);
    httpMock.verify();
  });
});
