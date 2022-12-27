import {TestBed} from '@angular/core/testing';

import {ShoppingListService} from './shopping-list.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {ShoppingList} from "../models/ShoppingList";
import {EStatus} from "../enums/EStatus";

describe('ShoppingListService', () => {
  let service: ShoppingListService;
  let httpMock: HttpTestingController;

  const testShoppingList: ShoppingList = {
    executionDate: new Date(),
    id: 1,
    isEdited: false,
    showItems: false,
    status: EStatus.WAITING,
    title: ""
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ]
    });
    service = TestBed.inject(ShoppingListService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getShoppingLists should GET on shopping-list', (done) => {
    service.getShoppingLists().subscribe(res => done());
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-list/shopping-list');
    expect(successRequest.request.method).toEqual('GET');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('addShoppingList should POST on shopping-list', (done) => {
    service.addShoppingList(testShoppingList).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-list/shopping-list');
    expect(successRequest.request.method).toEqual('POST');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('updateShoppingList should PUT on shopping-list/:id', (done) => {
    service.updateShoppingList(1, testShoppingList).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-list/shopping-list/1');
    expect(successRequest.request.method).toEqual('PUT');
    successRequest.flush(null);
    httpMock.verify();
  });

  it('deleteShoppingList should DELETE on shopping-list/:id', (done) => {
    service.deleteShoppingList(1).subscribe(res => done())
    const successRequest = httpMock.expectOne('http://localhost:8080/shopping-list/shopping-list/1');
    expect(successRequest.request.method).toEqual('DELETE');
    successRequest.flush(null);
    httpMock.verify();
  });
});
