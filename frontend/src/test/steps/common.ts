import { config as testConfig } from '../config';

declare const inject: () => { I: any };
declare const Given: (pattern: string, step: (...args: string[]) => void) => void;
declare const When: (pattern: string, step: (...args: string[]) => void) => void;
declare const Then: (pattern: string, step: (...args: string[]) => void) => void;

const { I } = inject();

export const iAmOnPage = (text: string): void => {
  const url = new URL(text, testConfig.TEST_URL);
  if (!url.searchParams.has('lng')) {
    url.searchParams.set('lng', 'en');
  }
  I.amOnPage(url.toString());
};
Given('I go to {string}', iAmOnPage);

Then('the page URL should be {string}', (url: string) => {
  I.waitInUrl(url);
});

Then('the page should include {string}', (text: string) => {
  I.waitForText(text);
});

When('I click {string}', (text: string) => {
  I.click(text);
});

When('I fill {string} with {string}', (field: string, value: string) => {
  I.fillField(field, value);
});

When('I select {string} as {string}', (field: string, value: string) => {
  I.selectOption(field, value);
});

Then('the page heading should be {string}', (heading: string) => {
  I.see(heading, 'h1');
});
