describe('Test sign in', () => {
  it('should redirect to login page', () => {
    cy.visit('/')
    cy.url().should('includes', 'login')
  })
  it('should have disabled login button', () => {
    cy.get('#submit-login-button')
      .should('be.disabled')
  })
  it('should login', () => {
    cy.fixture('login-data').then((data) => {
      const {username, password} = data
      cy.get('input[formcontrolname="username"]').type(username)
      cy.get('input[formcontrolname="password"]').type(password)
      cy.get('#submit-login-button')
        .should('not.be.disabled')
      cy.get('#submit-login-button').click()
      cy.url().should('include', 'shopping-list')
    })
  })
  it('should save token in sessionStorage', () => {
    cy.window().its("sessionStorage")
      .invoke("getItem", "auth-token")
      .should("exist")
  })
})
describe('Test add shopping list', () => {
  it('should have enable add button', () => {
    cy.visit('/shopping-list')
    cy.get('#addEmptyShoppingListButton')
      .should('not.be.disabled')
    cy.get('#addEmptyShoppingListButton').click()
  })
  it('should have disable save list button', () => {
    cy.get('#updateShoppingListButton')
      .should('be.disabled')
  })
  it('should add shopping list', () => {
    cy.fixture('shopping-list-data').then((data) => {
      const {title, executionDate} = data
      cy.get('#inputExecutionDate').clear()
      cy.get('#inputExecutionDate').type(executionDate)
      cy.get('#inputTitle').type(title)
      cy.get('#updateShoppingListButton')
        .should('not.be.disabled')
      cy.get('#updateShoppingListButton').click()
      cy.get('.shopping-list-title').last().should('have.text', title)
    })
  })
})
describe('Test edit shopping list', () => {
  it('should have enable edit button', () => {
    cy.get('#editShoppingListButton')
      .should('not.be.disabled')
    cy.get('#editShoppingListButton').click()
  })
  it('should edit shopping list', () => {
    cy.fixture('shopping-list-edit-data').then((data) => {
      const {title} = data
      cy.get('#inputTitle').clear()
      cy.get('#inputTitle').type(title)
      cy.get('#updateShoppingListButton')
        .should('not.be.disabled')
      cy.get('#updateShoppingListButton').click()
      cy.get('.shopping-list-title').first().should('have.text', title)
    })
  })
})
describe('Test check shopping list', () => {
  it('should have enable check checkbox', () => {
    cy.get('.checkbox-list-completed').first()
      .should('not.be.disabled')
    cy.get('.checkbox-list-completed').first().find('input').click({force:true});
  })
  it('should disable edit and delete buttons', () => {
    cy.get('#editShoppingListButton').first()
      .should('be.disabled')
    cy.get('#deleteShoppingListButton').first()
      .should('be.disabled')
  })
})
describe('Test uncheck shopping list', () => {
  it('should have enable check checkbox', () => {
    cy.get('.checkbox-list-completed').first()
      .should('not.be.disabled')
    cy.get('.checkbox-list-completed').first().find('input').click({force:true});
  })
  it('should enable edit and delete buttons', () => {
    cy.get('#editShoppingListButton').first()
      .should('not.be.disabled')
    cy.get('#deleteShoppingListButton').first()
      .should('not.be.disabled')
  })
})
describe('Test add shopping item', () => {
  it('should have expand list button', () => {
    cy.get('#loadShoppingItemsListButton').first()
      .should('exist')
    cy.get('#loadShoppingItemsListButton').first().click()
  })
  it('should have add item button', () => {
    cy.get('#addEmptyShoppingItemButton')
      .should('exist')
    cy.get('#addEmptyShoppingItemButton').click()
  })
  it('should have disable save item button', () => {
    cy.get('#updateShoppingItemButton')
      .should('be.disabled')
  })
  it('should add shopping item', () => {
    cy.fixture('shopping-item-data').then((data) => {
      const {name, quantity} = data
      cy.get('#quantity-input').type(quantity)
      cy.get('#name-input').type(name)
      cy.get('#item-unit-select').click().get('mat-option').first().click()
      cy.get('#updateShoppingItemButton')
        .should('not.be.disabled')
      cy.get('#updateShoppingItemButton').click()
      cy.get('.shopping-item-title').last().should('have.text', name)
    })
  })
})
describe('Test edit shopping item', () => {
  it('should have enable edit button', () => {
    cy.get('#editShoppingItemButton').first()
      .should('not.be.disabled')
    cy.get('#editShoppingItemButton').first().click()
  })
  it('should edit shopping item', () => {
    cy.fixture('shopping-item-edit-data').then((data) => {
      const {name, quantity} = data
      cy.get('#name-input').clear()
      cy.get('#name-input').type(name)
      cy.get('#quantity-input').type(quantity)
      cy.get('#updateShoppingItemButton')
        .should('not.be.disabled')
      cy.get('#updateShoppingItemButton').click()
      cy.get('.shopping-item-title').first().should('have.text', name)
    })
  })
})
describe('Test check shopping item', () => {
  it('should have enable check checkbox', () => {
    cy.get('.checkbox-item-completed').first()
      .should('not.be.disabled')
    cy.get('.checkbox-item-completed').first().find('input').click({force:true});
  })
  it('should disable edit and delete buttons', () => {
    cy.get('#editShoppingItemButton').first()
      .should('be.disabled')
    cy.get('#deleteShoppingItemButton').first()
      .should('be.disabled')
  })
})
describe('Test uncheck shopping item', () => {
  it('should have enable check checkbox', () => {
    cy.get('.checkbox-item-completed').first()
      .should('not.be.disabled')
    cy.get('.checkbox-item-completed').first().find('input').click({force:true});
  })
  it('should enable edit and delete buttons', () => {
    cy.get('#editShoppingItemButton').first()
      .should('not.be.disabled')
    cy.get('#deleteShoppingItemButton').first()
      .should('not.be.disabled')
  })
})
describe('Test delete shopping item', () => {
  it('should have enable delete button', () => {
    cy.get('#deleteShoppingItemButton').first()
      .should('not.be.disabled')
  })
  it("should delete element", () => {
    const deletedItem = cy.get('.shopping-item-card').first()
    cy.get('#deleteShoppingItemButton').first().click()
    deletedItem.should('not.exist')
  })
})
describe('Test delete shopping list', () => {
  it('should have enable delete button', () => {
    cy.get('#deleteShoppingListButton').first()
      .should('not.be.disabled')
  })
  it("should delete element", () => {
    const deletedList = cy.get('.shopping-list-card').first()
    cy.get('#deleteShoppingListButton').first().click()
    deletedList.should('not.exist')
  })
})
describe('Test sign out', () => {
  it('should have enabled profile button', () => {
    cy.get('#profile-button')
      .should('not.be.disabled')
    cy.get('#profile-button').click()
  })
  it('should have logout button', () => {
    cy.get('#logout-button')
      .should('exist')
    cy.get('#logout-button').click()
  })
  it('should redirect to login page', () => {
    cy.url().should('includes', 'login')
  })
})
describe('Test sign in user not exists', () => {
  it('should redirect to login page', () => {
    cy.visit('/')
    cy.url().should('includes', 'login')
  })
  it('should have disabled login button', () => {
    cy.get('#submit-login-button')
      .should('be.disabled')
  })
  it('should not login', () => {
    cy.fixture('login-data-error').then((data) => {
      const {username, password} = data
      cy.get('input[formcontrolname="username"]').type(username)
      cy.get('input[formcontrolname="password"]').type(password)
      cy.get('#submit-login-button')
        .should('not.be.disabled')
      cy.get('#submit-login-button').click()
      cy.url().should('include', 'login')
    })
  })
})
