# Flashcard Service API

<details>
  <summary><strong>Table of Contents</strong></summary>

  * [Query](#query)
  * [Mutation](#mutation)
  * [Objects](#objects)
    * [Flashcard](#flashcard)
    * [FlashcardLearnedFeedback](#flashcardlearnedfeedback)
    * [FlashcardProgressData](#flashcardprogressdata)
    * [FlashcardProgressDataLog](#flashcardprogressdatalog)
    * [FlashcardSet](#flashcardset)
    * [FlashcardSetMutation](#flashcardsetmutation)
    * [FlashcardSetProgress](#flashcardsetprogress)
    * [FlashcardSide](#flashcardside)
    * [PaginationInfo](#paginationinfo)
  * [Inputs](#inputs)
    * [CreateFlashcardInput](#createflashcardinput)
    * [CreateFlashcardSetInput](#createflashcardsetinput)
    * [DateTimeFilter](#datetimefilter)
    * [FlashcardSideInput](#flashcardsideinput)
    * [IntFilter](#intfilter)
    * [LogFlashcardLearnedInput](#logflashcardlearnedinput)
    * [LogFlashcardSetLearnedInput](#logflashcardsetlearnedinput)
    * [Pagination](#pagination)
    * [StringFilter](#stringfilter)
    * [UpdateFlashcardInput](#updateflashcardinput)
  * [Enums](#enums)
    * [SortDirection](#sortdirection)
  * [Scalars](#scalars)
    * [Boolean](#boolean)
    * [Date](#date)
    * [DateTime](#datetime)
    * [Float](#float)
    * [Int](#int)
    * [JSON](#json)
    * [LocalTime](#localtime)
    * [String](#string)
    * [Time](#time)
    * [UUID](#uuid)
    * [Url](#url)

</details>

## Query
<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>flashcardsByIds</strong></td>
<td valign="top">[<a href="#flashcard">Flashcard</a>!]!</td>
<td>


Get flashcards by their ids.
🔒 The user must be enrolled in the course the flashcards belong to. Otherwise an error is thrown.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">ids</td>
<td valign="top">[<a href="#uuid">UUID</a>!]!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>findFlashcardSetsByAssessmentIds</strong></td>
<td valign="top">[<a href="#flashcardset">FlashcardSet</a>]!</td>
<td>


Get flashcard sets by their assessment ids.
Returns a list of flashcard sets in the same order as the provided ids.
Each element is null if the corresponding id is not found.
🔒 The user must be enrolled in the course the flashcard sets belong to. Otherwise for that element null is returned.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">assessmentIds</td>
<td valign="top">[<a href="#uuid">UUID</a>!]!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>dueFlashcardsByCourseId</strong></td>
<td valign="top">[<a href="#flashcard">Flashcard</a>!]!</td>
<td>


Get flashcards of a course that are due to be reviewed.
🔒 The user must be enrolled in the course the flashcards belong to. Otherwise an error is thrown.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">courseId</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
</tbody>
</table>

## Mutation

Mutations for the flashcard service. Provides mutations for creating, updating, and deleting flashcard as well as
creating and deleting flashcard sets. To update a flashcard set, update, delete, and create flashcards individually.

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>_internal_noauth_createFlashcardSet</strong></td>
<td valign="top"><a href="#flashcardset">FlashcardSet</a>!</td>
<td>


Creates a new flashcard set. Mutation is only accessible internally within the system by other
services and the gateway.
⚠️ This mutation is only accessible internally in the system and allows the caller to create FlashcardSets without
any permissions check and should not be called without any validation of the caller's permissions. ⚠️

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">courseId</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">assessmentId</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">input</td>
<td valign="top"><a href="#createflashcardsetinput">CreateFlashcardSetInput</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>deleteFlashcardSet</strong> ⚠️</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


Deletes a flashcard set. Throws an error if the flashcard set does not exist.
The contained flashcards are deleted as well.

<p>⚠️ <strong>DEPRECATED</strong></p>
<blockquote>

Only for development, will be removed in production. Use deleteAssessment in contents service instead.

</blockquote>
</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">input</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>mutateFlashcardSet</strong></td>
<td valign="top"><a href="#flashcardsetmutation">FlashcardSetMutation</a>!</td>
<td>


Modify a flashcard set.
🔒 The user must be an admin the course the flashcard set is in to perform this action.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">assessmentId</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>logFlashcardLearned</strong></td>
<td valign="top"><a href="#flashcardlearnedfeedback">FlashcardLearnedFeedback</a>!</td>
<td>


Logs that a user has learned a flashcard.
🔒 The user must be enrolled in the course the flashcard set is in to perform this action.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">input</td>
<td valign="top"><a href="#logflashcardlearnedinput">LogFlashcardLearnedInput</a>!</td>
<td></td>
</tr>
</tbody>
</table>

## Objects

### Flashcard


A flashcard is a set of two or more sides. Each side has a label and a text.
The label is used to specify which side of the flashcard is being shown to the user first for learning
and which sides he has to guess.

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>id</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


Unique identifier of this flashcard.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>sides</strong></td>
<td valign="top">[<a href="#flashcardside">FlashcardSide</a>!]!</td>
<td>


List of sides of this flashcard.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>userProgressData</strong></td>
<td valign="top"><a href="#flashcardprogressdata">FlashcardProgressData</a>!</td>
<td>


Progress data of the flashcard, specific to given users.
If userId is not provided, the progress data of the current user is returned.

</td>
</tr>
</tbody>
</table>

### FlashcardLearnedFeedback


Feedback for the logFlashcardLearned mutation.

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>success</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


Whether the flashcard was learned correctly.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>nextLearnDate</strong></td>
<td valign="top"><a href="#datetime">DateTime</a>!</td>
<td>


Next date when the flashcard should be learned again.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>flashcardSetProgress</strong></td>
<td valign="top"><a href="#flashcardsetprogress">FlashcardSetProgress</a>!</td>
<td>


Progress of the whole flashcard set.

</td>
</tr>
</tbody>
</table>

### FlashcardProgressData

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>lastLearned</strong></td>
<td valign="top"><a href="#datetime">DateTime</a></td>
<td>


The date the user learned the flashcard.
This is null it the user has not learned the content item once.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>learningInterval</strong></td>
<td valign="top"><a href="#int">Int</a></td>
<td>


The learning interval in days for the content item.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>nextLearn</strong></td>
<td valign="top"><a href="#datetime">DateTime</a></td>
<td>


The next time the content should be learned.
Calculated using the date the user completed the content item and the learning interval.
This is null if the user has not completed the content item once.

</td>
</tr>
</tbody>
</table>

### FlashcardProgressDataLog

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>id</strong></td>
<td valign="top"><a href="#uuid">UUID</a></td>
<td>


The id of the Log

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>learnedAt</strong></td>
<td valign="top"><a href="#datetime">DateTime</a>!</td>
<td>


The date the user learned the flashcard.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>success</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


Whether the user knew the flashcard or not.

</td>
</tr>
</tbody>
</table>

### FlashcardSet


A set of flashcards. A flashcard set belongs to exactly one assessment. Therefore, the uuid of the assessment
also serves as the identifier of a flashcard set.

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>assessmentId</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


The uuid of the assessment this flashcard set belongs to.
This also serves as the identifier of this flashcard set.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>courseId</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


Id of the course this flashcard set belongs to.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>flashcards</strong></td>
<td valign="top">[<a href="#flashcard">Flashcard</a>!]!</td>
<td>


List of flashcards in this set.

</td>
</tr>
</tbody>
</table>

### FlashcardSetMutation

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>assessmentId</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


ID of the flashcard set that is being modified.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>createFlashcard</strong></td>
<td valign="top"><a href="#flashcard">Flashcard</a>!</td>
<td>


Creates a new flashcard. Throws an error if the flashcard set does not exist.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">input</td>
<td valign="top"><a href="#createflashcardinput">CreateFlashcardInput</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>updateFlashcard</strong></td>
<td valign="top"><a href="#flashcard">Flashcard</a>!</td>
<td>


Updates a flashcard. Throws an error if the flashcard does not exist.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">input</td>
<td valign="top"><a href="#updateflashcardinput">UpdateFlashcardInput</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>deleteFlashcard</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


Deletes the flashcard with the specified ID. Throws an error if the flashcard does not exist.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">id</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
</tbody>
</table>

### FlashcardSetProgress

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>percentageLearned</strong></td>
<td valign="top"><a href="#float">Float</a>!</td>
<td>


Percentage of how many flashcards in the set have been learned.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>correctness</strong></td>
<td valign="top"><a href="#float">Float</a>!</td>
<td>


Percentage of how many flashcards have been learned correctly of the ones that have been learned.

</td>
</tr>
</tbody>
</table>

### FlashcardSide

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>text</strong></td>
<td valign="top"><a href="#json">JSON</a>!</td>
<td>


Text of this flashcard side as rich text in SlateJS json.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>label</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td>


Label of this flashcard side. E.g. "Front" or "Back", or "Question" or "Answer".

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>isQuestion</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


Whether this side is a question, i.e. should be shown to the user to guess the other sides or not.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>isAnswer</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


Whether this side is also an answer. Some Flashcards can have their sides be used as both questions or answers for the other sides

</td>
</tr>
</tbody>
</table>

### PaginationInfo


Return type for information about paginated results.

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>page</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>


The current page number.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>size</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>


The number of elements per page.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>totalElements</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>


The total number of elements across all pages.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>totalPages</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>


The total number of pages.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>hasNext</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


Whether there is a next page.

</td>
</tr>
</tbody>
</table>

## Inputs

### CreateFlashcardInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>sides</strong></td>
<td valign="top">[<a href="#flashcardsideinput">FlashcardSideInput</a>!]!</td>
<td>


List of sides of this flashcard. Must be at least two sides.

</td>
</tr>
</tbody>
</table>

### CreateFlashcardSetInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>flashcards</strong></td>
<td valign="top">[<a href="#createflashcardinput">CreateFlashcardInput</a>!]!</td>
<td>


List of flashcards in this set.

</td>
</tr>
</tbody>
</table>

### DateTimeFilter


Filter for date values.
If multiple filters are specified, they are combined with AND.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>after</strong></td>
<td valign="top"><a href="#datetime">DateTime</a></td>
<td>


If specified, filters for dates after the specified value.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>before</strong></td>
<td valign="top"><a href="#datetime">DateTime</a></td>
<td>


If specified, filters for dates before the specified value.

</td>
</tr>
</tbody>
</table>

### FlashcardSideInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>text</strong></td>
<td valign="top"><a href="#json">JSON</a>!</td>
<td>


Text of this flashcard side.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>label</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td>


Label of this flashcard side. E.g. "Front" or "Back", or "Question" or "Answer".

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>isQuestion</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


Whether this side is a question, i.e. should be shown to the user to guess the other sides or not.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>isAnswer</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


Whether this side is also an answer. Some Flashcards can have their sides be used as both questions or answers for the other sides

</td>
</tr>
</tbody>
</table>

### IntFilter


Filter for integer values.
If multiple filters are specified, they are combined with AND.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>equals</strong></td>
<td valign="top"><a href="#int">Int</a></td>
<td>


An integer value to match exactly.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>greaterThan</strong></td>
<td valign="top"><a href="#int">Int</a></td>
<td>


If specified, filters for values greater than to the specified value.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>lessThan</strong></td>
<td valign="top"><a href="#int">Int</a></td>
<td>


If specified, filters for values less than to the specified value.

</td>
</tr>
</tbody>
</table>

### LogFlashcardLearnedInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>flashcardId</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


The id of the flashcard that was learned.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>successful</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


If the user knew the flashcard or not.

</td>
</tr>
</tbody>
</table>

### LogFlashcardSetLearnedInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>flashcardSetId</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


The id of the flashcard that was learned.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>userId</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


The id of the user that learned the flashcard.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>percentageSuccess</strong></td>
<td valign="top"><a href="#float">Float</a>!</td>
<td>


The percentage of flashcards in the set that the user knew.

</td>
</tr>
</tbody>
</table>

### Pagination


Specifies the page size and page number for paginated results.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>page</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>


The page number, starting at 0.
If not specified, the default value is 0.
For values greater than 0, the page size must be specified.
If this value is larger than the number of pages, an empty page is returned.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>size</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>


The number of elements per page.

</td>
</tr>
</tbody>
</table>

### StringFilter


Filter for string values.
If multiple filters are specified, they are combined with AND.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>equals</strong></td>
<td valign="top"><a href="#string">String</a></td>
<td>


A string value to match exactly.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>contains</strong></td>
<td valign="top"><a href="#string">String</a></td>
<td>


A string value that must be contained in the field that is being filtered.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>ignoreCase</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>


If true, the filter is case-insensitive.

</td>
</tr>
</tbody>
</table>

### UpdateFlashcardInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong>id</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td>


Id of the flashcard to update.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong>sides</strong></td>
<td valign="top">[<a href="#flashcardsideinput">FlashcardSideInput</a>!]!</td>
<td>


List of sides of this flashcard. Must be at least two sides.

</td>
</tr>
</tbody>
</table>

## Enums

### SortDirection


Specifies the sort direction, either ascending or descending.

<table>
<thead>
<th align="left">Value</th>
<th align="left">Description</th>
</thead>
<tbody>
<tr>
<td valign="top"><strong>ASC</strong></td>
<td></td>
</tr>
<tr>
<td valign="top"><strong>DESC</strong></td>
<td></td>
</tr>
</tbody>
</table>

## Scalars

### Boolean

Built-in Boolean

### Date

An RFC-3339 compliant Full Date Scalar

### DateTime

A slightly refined version of RFC-3339 compliant DateTime Scalar

### Float

Built-in Float

### Int

Built-in Int

### JSON

A JSON scalar

### LocalTime

24-hour clock time value string in the format `hh:mm:ss` or `hh:mm:ss.sss`.

### String

Built-in String

### Time

An RFC-3339 compliant Full Time Scalar

### UUID

A universally unique identifier compliant UUID Scalar

### Url

A Url scalar

