/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.test.model;

import java.util.List;
import java.util.Map;

import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTreeNavigator;
import ru.orangesoftware.financisto2.test.builders.CategoryBuilder;
import ru.orangesoftware.financisto2.test.db.AbstractDbTest;

public class CategoryTreeNavigatorTest extends AbstractDbTest {

    Map<String,Category> categories;
    CategoryTreeNavigator navigator;    

    @Override
    public void setUp() throws Exception {
        super.setUp();
        /**
         * A
         * - A1
         * -- AA1
         * - A2
         * B
         */
        categories = CategoryBuilder.createDefaultHierarchy(categoryRepository);
        CategoryRepository repository = new CategoryRepository(context);
        repository.db = db;
        navigator = new CategoryTreeNavigator(context, repository);
    }
    
    public void test_should_add_expense_income_level() {
        navigator.separateIncomeAndExpense();
        assertSelected(Category.NO_CATEGORY_ID, "No category", "<INCOME>", "<EXPENSE>");

        navigator.navigateTo(CategoryTreeNavigator.EXPENSE_CATEGORY_ID);
        assertSelected(CategoryTreeNavigator.EXPENSE_CATEGORY_ID, "<EXPENSE>", "A");

        navigator.navigateTo(categories.get("A").id);
        assertSelected(categories.get("A").id, "A", "A1", "A2");

        navigator.goBack();
        assertSelected(Category.NO_CATEGORY_ID, "<EXPENSE>", "A");

        navigator.goBack();
        assertSelected(Category.NO_CATEGORY_ID, "No category", "<INCOME>", "<EXPENSE>");
    }

    public void test_should_select_startup_category() {
        long selectedCategoryId = categories.get("AA1").id;
        navigator.selectCategory(selectedCategoryId);
        assertEquals(selectedCategoryId, navigator.selectedCategoryId);
        assertSelected(selectedCategoryId, "A1", "AA1");
    }
    
    public void test_should_navigate_to_category() {
        long categoryId = categories.get("A").id;
        navigator.navigateTo(categoryId);
        assertSelected(categoryId, "A", "A1", "A2");

        categoryId = categories.get("A1").id;
        navigator.navigateTo(categoryId);
        assertSelected(categoryId, "A1", "AA1");

        categoryId = categories.get("AA1").id;
        navigator.navigateTo(categoryId);
        assertSelected(categoryId, "A1", "AA1");
    }

    public void test_should_select_parent_category_when_navigating_back() {
        long categoryId = categories.get("AA1").id;
        navigator.selectCategory(categoryId);
        assertSelected(categoryId, "A1", "AA1");
        assertTrue(navigator.canGoBack());

        assertTrue(navigator.goBack());
        assertSelected(categories.get("A1").id, "A", "A1", "A2");
        assertTrue(navigator.canGoBack());

        assertTrue(navigator.goBack());
        assertSelected(categories.get("A").id, "No category", "A", "B");
        assertFalse(navigator.canGoBack());

        assertFalse(navigator.goBack());
    }

    private void assertSelected(long selectedCategoryId, String... categories) {
        assertEquals(selectedCategoryId, navigator.selectedCategoryId);
        List<Category> roots = navigator.getSelectedRoots();
        assertEquals("Got too many or too few categories", categories.length, roots.size());
        for (int i=0; i<categories.length; i++) {
            assertEquals("Unexpected category on index "+i, categories[i], roots.get(i).title);
        }
    }


}
