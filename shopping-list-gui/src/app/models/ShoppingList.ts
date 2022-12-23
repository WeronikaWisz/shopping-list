import {EStatus} from "../enums/EStatus";

export interface ShoppingList {
  id: number;
  title: string;
  executionDate: Date;
  status: EStatus;
  isEdited: boolean;
  showItems: boolean;
}
