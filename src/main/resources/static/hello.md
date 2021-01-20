Hi there! Welcome to `inodes`. This is a website to hosts your frequently used scripts, instances, notes. It's highly customizable (you can script it) so you can store literally store any data (as long as you can to encode it as a string).

Here are some notes for the users, explore the website and enjoy!

### Searching | Content
All the content is indexed as text by following fields
1. owner-name
2. tags
3. content
4. content-type

You can search / filter the content by any of the fields. Here are the filters you should know.
* `~username` : username filter (Clicking on username tag works too)
* `%content-type` : content-type filter (Clicking on content-type tag works too)
* `#tag` : tag filter (Clicking on the tag works too)
* Any other tokens are considered as content search items.

> **Note:**
> 1. You can CTRL + click on the tags to apply multiple filters.
> 2. For permalinks, click on the `#` in the actions list and copy the URL from address bar.

### New content
Currently we support posting the below contents

1. Posts
    * Notes about something
    * tiny scripts
    * commands
    * [markdown](https://guides.github.com/features/mastering-markdown/) is supported

2. Instances
    * Share your repro instances with
        * Host details
        * App URLs with credentials

3. **Applets**:  Do you have a small, simple app which is not small enough to fit in to a card of this website. Go ahead post it here. Here are few examples (Click on `edit` to see the source code.
    * TSFTP zip and downloader [Link](?q=@bc412532-c4ed-49dc-8da1-4e91adb7336c)
    * EDC resource creator [Link](?q=@95ae7622-f09f-412c-a9ac-691fb25548f6)


### Privileges
The authorization is simple.
1. Everyone has view privileges on public content.

2. People with `CREATE` privileges can post. You can get `CREATE` privilege by asking someone who has `CREATE` privilege to approve you as a editor.

3. People with `EDIT` privileges can edit others content. Others can edit only their own content. You can get this privilege by getting approved by a `EDIT`or.

4. Any logged-in user can `upvote`, `downvote`, `comment`, `save` posts.


> #### Note of caution
> Be responsible when approving someone or choosing the approver. Else the whole tree under you will loose the privileges, you are assigning.


### New templates (new content types)
Post a `post` with the details of the new content and add the tags `featurerequest` and `mmp`. I will pick it and get back to you. Or directly write to me
