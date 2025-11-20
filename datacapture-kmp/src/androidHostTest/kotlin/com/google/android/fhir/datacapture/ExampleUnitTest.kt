/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.datacapture

import com.google.fhir.model.r4.FhirR4Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun yellow() {
    val json =
      """
            {
              "resourceType": "Questionnaire",
              "status": "active",
              "item": [
                {
                  "linkId": "1",
                  "type": "string",
                  "item": [
                    {
                      "linkId": "1.1",
                      "text": "Enter a string",
                      "type": "display",
                      "extension": [
                        {
                          "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
                          "valueCodeableConcept": {
                            "coding": [
                              {
                                "system": "http://hl7.org/fhir/questionnaire-item-control",
                                "code": "flyover",
                                "display": "Fly-over"
                              }
                            ],
                            "text": "Flyover"
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  "linkId": "2",
                  "type": "integer",
                  "item": [
                    {
                      "linkId": "2.1",
                      "text": "Enter an integer",
                      "type": "display",
                      "extension": [
                        {
                          "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
                          "valueCodeableConcept": {
                            "coding": [
                              {
                                "system": "http://hl7.org/fhir/questionnaire-item-control",
                                "code": "flyover",
                                "display": "Fly-over"
                              }
                            ],
                            "text": "Flyover"
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  "linkId": "3",
                  "type": "decimal",
                  "item": [
                    {
                      "linkId": "3.1",
                      "text": "Enter a decimal",
                      "type": "display",
                      "extension": [
                        {
                          "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
                          "valueCodeableConcept": {
                            "coding": [
                              {
                                "system": "http://hl7.org/fhir/questionnaire-item-control",
                                "code": "flyover",
                                "display": "Fly-over"
                              }
                            ],
                            "text": "Flyover"
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  "linkId": "4",
                  "type": "integer",
                  "extension": [
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
                      "valueCoding": {
                        "system": "http://unitsofmeasure.org",
                        "code": "kg",
                        "display": "kilogram"
                      }
                    }
                  ],
                  "item": [
                    {
                      "linkId": "4.1",
                      "text": "Enter an integer (with unit)",
                      "type": "display",
                      "extension": [
                        {
                          "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
                          "valueCodeableConcept": {
                            "coding": [
                              {
                                "system": "http://hl7.org/fhir/questionnaire-item-control",
                                "code": "flyover",
                                "display": "Fly-over"
                              }
                            ],
                            "text": "Flyover"
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  "linkId": "5",
                  "type": "decimal",
                  "extension": [
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
                      "valueCoding": {
                        "system": "http://unitsofmeasure.org",
                        "code": "kg",
                        "display": "kilogram"
                      }
                    }
                  ],
                  "item": [
                    {
                      "linkId": "5.1",
                      "text": "Enter a decimal (with unit)",
                      "type": "display",
                      "extension": [
                        {
                          "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
                          "valueCodeableConcept": {
                            "coding": [
                              {
                                "system": "http://hl7.org/fhir/questionnaire-item-control",
                                "code": "flyover",
                                "display": "Fly-over"
                              }
                            ],
                            "text": "Flyover"
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  "linkId": "6",
                  "type": "text",
                  "item": [
                    {
                      "linkId": "6.1",
                      "text": "Enter text (multiline)",
                      "type": "display",
                      "extension": [
                        {
                          "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
                          "valueCodeableConcept": {
                            "coding": [
                              {
                                "system": "http://hl7.org/fhir/questionnaire-item-control",
                                "code": "flyover",
                                "display": "Fly-over"
                              }
                            ],
                            "text": "Flyover"
                          }
                        }
                      ]
                    }
                  ]
                },
                {
                  "linkId": "7",
                  "type": "string",
                  "readOnly": true,
                  "item": [
                    {
                      "linkId": "7.1",
                      "text": "Enter a string (readonly)",
                      "type": "display",
                      "extension": [
                        {
                          "url": "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl",
                          "valueCodeableConcept": {
                            "coding": [
                              {
                                "system": "http://hl7.org/fhir/questionnaire-item-control",
                                "code": "flyover",
                                "display": "Fly-over"
                              }
                            ],
                            "text": "Flyover"
                          }
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """
        .trimIndent()
    val question = FhirR4Json().decodeFromString(json)
    assertNotNull(question)
  }
}
