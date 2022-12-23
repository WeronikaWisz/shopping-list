import {EStatus} from "../enums/EStatus";
import {EUnit} from "../enums/EUnit";

export interface ShoppingItem {
  id: number;
  name: string;
  quantity: number;
  unit: EUnit;
  status: EStatus;
  shoppingListId: number;
  isEdited: boolean;
  hasImage: boolean;
}
